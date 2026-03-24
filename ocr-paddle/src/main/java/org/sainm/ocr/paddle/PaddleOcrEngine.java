package org.sainm.ocr.paddle;

import org.sainm.exception.OcrException;
import org.sainm.model.BoundingBox;
import org.sainm.model.TextBlock;
import org.sainm.ocr.paddle.ffi.PaddleBindings;
import org.sainm.ocr.paddle.model.OcrResult;

import java.awt.image.BufferedImage;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PaddleOcrEngine implements AutoCloseable {

    private static final int DET_MAX_SIDE = 960;
    private static final int REC_HEIGHT = 48;
    private static final int REC_MAX_WIDTH = 320;
    private static final int CPU_THREADS = Integer.getInteger("pdf.compare.ocr.paddle.cpuThreads", 1);
    private static final boolean ENABLE_MKLDNN =
            Boolean.parseBoolean(System.getProperty("pdf.compare.ocr.paddle.enableMkldnn", "false"));
    private static final boolean ENABLE_IR_OPTIM =
            Boolean.parseBoolean(System.getProperty("pdf.compare.ocr.paddle.enableIrOptim", "false"));
    private static final boolean ENABLE_MEMORY_OPTIM =
            Boolean.parseBoolean(System.getProperty("pdf.compare.ocr.paddle.enableMemoryOptim", "false"));

    private final MemorySegment detPredictor;
    private final MemorySegment recPredictor;
    private final PostProcessor postProcessor;
    private final Arena arena;
    private volatile boolean closed;

    private PaddleOcrEngine(MemorySegment detPredictor,
                            MemorySegment recPredictor,
                            PostProcessor postProcessor,
                            Arena arena) {
        this.detPredictor = detPredictor;
        this.recPredictor = recPredictor;
        this.postProcessor = postProcessor;
        this.arena = arena;
    }

    public static PaddleOcrEngine create(Path modelDir, String language) {
        if (!PaddleBindings.isAvailable()) {
            throw new OcrException("Paddle Inference native library not available");
        }

        var spec = modelSpec(language);
        var detDir = modelDir.resolve(spec.detDir());
        var recDir = modelDir.resolve(spec.recDir());
        var dictFile = modelDir.resolve(spec.dictFile());

        validateModelDir(detDir, "detection");
        validateModelDir(recDir, "recognition");

        var arena = Arena.ofShared();
        try {
            var detPredictor = createPredictor(arena, detDir);
            var recPredictor = createPredictor(arena, recDir);

            PostProcessor pp;
            if (Files.exists(dictFile)) {
                pp = PostProcessor.fromDictFile(dictFile);
            } else {
                pp = "ja".equals(spec.language()) ? PostProcessor.defaultJapanese() : PostProcessor.defaultChinese();
            }

            return new PaddleOcrEngine(detPredictor, recPredictor, pp, arena);
        } catch (Exception e) {
            arena.close();
            throw new OcrException("Failed to initialize Paddle OCR engine", e);
        }
    }

    public List<TextBlock> recognize(BufferedImage image, int pageNumber, double minConfidence) {
        if (closed) throw new OcrException("Engine is closed");

        var detResult = ImagePreprocessor.forDetection(image, DET_MAX_SIDE);
        float[] detOutput = runDetection(detResult.data(), detResult.shape());
        int detH = detResult.shape()[2];
        int detW = detResult.shape()[3];
        List<float[][]> boxes = postProcessor.dbPostProcess(detOutput, detH, detW, detResult.ratio());

        if (boxes.isEmpty()) return List.of();

        List<TextBlock> results = new ArrayList<>();
        for (float[][] box : boxes) {
            BufferedImage cropped = PostProcessor.cropRegion(image, box);
            if (cropped.getWidth() < 2 || cropped.getHeight() < 2) continue;

            var recResult = ImagePreprocessor.forRecognition(cropped, REC_HEIGHT, REC_MAX_WIDTH);
            float[] recOutput = runRecognition(recResult.data(), recResult.shape());

            int seqLen = recResult.shape()[3] / 8;
            int vocabSize = recOutput.length / Math.max(seqLen, 1);
            if (vocabSize <= 0 || seqLen <= 0) continue;
            validateRecognitionVocabulary(vocabSize);

            OcrResult ocrResult = postProcessor.ctcDecode(recOutput, seqLen, vocabSize);
            if (ocrResult.text().isEmpty() || ocrResult.score() < minConfidence) continue;

            BoundingBox bbox = PostProcessor.toBoundingBox(box, pageNumber);
            var block = new TextBlock(ocrResult.text(), bbox);
            block.setConfidence(ocrResult.score());
            block.setSource(TextBlock.Source.OCR);
            results.add(block);
        }
        return results;
    }

    private float[] runDetection(float[] inputData, int[] shape) {
        try (var local = Arena.ofConfined()) {
            var inputTensor = PaddleBindings.predictorGetInputHandle(
                    detPredictor, PaddleBindings.allocateString(local, "x"));
            var shapeSegment = PaddleBindings.allocateIntArray(local, shape);
            PaddleBindings.tensorReshape(inputTensor, shape.length, shapeSegment);

            var dataSegment = PaddleBindings.allocateFloatArray(local, inputData);
            PaddleBindings.tensorCopyFromCpuFloat(inputTensor, dataSegment);

            PaddleBindings.predictorRun(detPredictor);

            var outputTensor = getOutputTensor(local, detPredictor, "sigmoid_0.tmp_0");

            int outputSize = shape[2] * shape[3];
            var outputSegment = local.allocate(ValueLayout.JAVA_FLOAT, outputSize);
            PaddleBindings.tensorCopyToCpuFloat(outputTensor, outputSegment);

            return PaddleBindings.readFloatArray(outputSegment, outputSize);
        }
    }

    private float[] runRecognition(float[] inputData, int[] shape) {
        try (var local = Arena.ofConfined()) {
            var inputTensor = PaddleBindings.predictorGetInputHandle(
                    recPredictor, PaddleBindings.allocateString(local, "x"));
            var shapeSegment = PaddleBindings.allocateIntArray(local, shape);
            PaddleBindings.tensorReshape(inputTensor, shape.length, shapeSegment);

            var dataSegment = PaddleBindings.allocateFloatArray(local, inputData);
            PaddleBindings.tensorCopyFromCpuFloat(inputTensor, dataSegment);

            PaddleBindings.predictorRun(recPredictor);

            var outputTensor = getRecognitionOutputTensor(local, recPredictor,
                    "softmax_0.tmp_0",
                    "softmax_1.tmp_0",
                    "softmax_2.tmp_0");

            int[] outShape = PaddleBindings.tensorReadShape(outputTensor);
            int shapeSize = outShape.length;
            int outputSize = 1;
            for (int i = 0; i < shapeSize; i++) {
                outputSize *= outShape[i];
            }

            var outputSegment = local.allocate(ValueLayout.JAVA_FLOAT, outputSize);
            PaddleBindings.tensorCopyToCpuFloat(outputTensor, outputSegment);

            return PaddleBindings.readFloatArray(outputSegment, outputSize);
        }
    }

    private MemorySegment getRecognitionOutputTensor(Arena arena,
                                                     MemorySegment predictor,
                                                     String... candidateNames) {
        MemorySegment fallback = null;
        int[] fallbackShape = null;
        OcrException last = null;

        for (String name : candidateNames) {
            try {
                var tensor = PaddleBindings.predictorGetOutputHandle(
                        predictor, PaddleBindings.allocateString(arena, name));
                int[] shape = PaddleBindings.tensorReadShape(tensor);
                if (shape.length == 3 && shape[0] == 1 && shape[2] > 256) {
                    return tensor;
                }
                if (fallback == null || isBetterRecognitionFallback(shape, fallbackShape)) {
                    fallback = tensor;
                    fallbackShape = shape;
                }
            } catch (OcrException e) {
                last = e;
            }
        }

        if (fallback != null) {
            return fallback;
        }
        throw new OcrException("No matching recognition output tensor found for predictor", last);
    }

    private boolean isBetterRecognitionFallback(int[] candidateShape, int[] currentShape) {
        if (currentShape == null) {
            return true;
        }
        if (candidateShape.length != currentShape.length) {
            return candidateShape.length > currentShape.length;
        }
        if (candidateShape.length == 0) {
            return false;
        }
        return candidateShape[candidateShape.length - 1] > currentShape[currentShape.length - 1];
    }

    private MemorySegment getOutputTensor(Arena arena, MemorySegment predictor, String... candidateNames) {
        OcrException last = null;
        for (String name : candidateNames) {
            try {
                return PaddleBindings.predictorGetOutputHandle(
                        predictor, PaddleBindings.allocateString(arena, name));
            } catch (OcrException e) {
                last = e;
            }
        }
        throw new OcrException("No matching output tensor found for predictor", last);
    }

    private static MemorySegment createPredictor(Arena arena, Path modelDir) {
        var modelFile = modelDir.resolve("inference.pdmodel");
        var paramsFile = modelDir.resolve("inference.pdiparams");

        var config = PaddleBindings.configCreate();
        PaddleBindings.configSetModel(config,
                PaddleBindings.allocateString(arena, modelFile.toString()),
                PaddleBindings.allocateString(arena, paramsFile.toString()));
        PaddleBindings.configDisableGpu(config);
        PaddleBindings.configSetCpuThreads(config, Math.max(CPU_THREADS, 1));
        if (ENABLE_MKLDNN) {
            PaddleBindings.configEnableMKLDNN(config);
        }
        PaddleBindings.configSwitchIrOptim(config, ENABLE_IR_OPTIM);
        PaddleBindings.configEnableMemoryOptim(config, ENABLE_MEMORY_OPTIM);
        return PaddleBindings.predictorCreate(config);
    }

    private static void validateModelDir(Path dir, String name) {
        if (!Files.isDirectory(dir)) {
            throw new OcrException(name + " model directory not found: " + dir);
        }
        if (!Files.exists(dir.resolve("inference.pdmodel"))
                || !Files.exists(dir.resolve("inference.pdiparams"))) {
            throw new OcrException(name + " model files missing in: " + dir);
        }
    }

    private void validateRecognitionVocabulary(int vocabSize) {
        int dictionarySize = postProcessor.dictionarySize();
        if (vocabSize == dictionarySize || vocabSize == dictionarySize + 1) {
            return;
        }
        throw new OcrException("Recognition dictionary size mismatch: vocabSize="
                + vocabSize + ", dictionarySize=" + dictionarySize);
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        try {
            PaddleBindings.predictorDestroy(detPredictor);
            PaddleBindings.predictorDestroy(recPredictor);
        } finally {
            arena.close();
        }
    }

    static ModelSpec modelSpec(String language) {
        if ("ja".equalsIgnoreCase(language) || "japan".equalsIgnoreCase(language)) {
            return new ModelSpec("ja", "ch_PP-OCRv4_det_infer", "japan_PP-OCRv4_rec_infer", "japan_dict.txt");
        }
        return new ModelSpec("zh", "ch_PP-OCRv4_det_infer", "ch_PP-OCRv4_rec_infer", "ppocr_keys_v1.txt");
    }

    record ModelSpec(String language, String detDir, String recDir, String dictFile) {}
}

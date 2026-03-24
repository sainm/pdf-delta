package org.sainm.ocr.paddle.ffi;

import org.sainm.exception.OcrException;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.file.Path;





public final class PaddleBindings {

    private static final long ONE_DIM_ARRAY_INT32_HEADER_BYTES =
            ValueLayout.JAVA_LONG.byteSize() + ValueLayout.ADDRESS.byteSize();

    private PaddleBindings() {}

    public static boolean isAvailable() {
        return PaddleHandles.isAvailable();
    }

    public static void loadLibrary(Path libraryPath) {
        PaddleHandles.loadLibrary(libraryPath);
    }

    

    public static MemorySegment configCreate() {
        try {
            return (MemorySegment) PaddleHandles.PD_ConfigCreate.invokeExact();
        } catch (Throwable t) {
            throw wrap("PD_ConfigCreate", t);
        }
    }

    public static void configDestroy(MemorySegment config) {
        try {
            PaddleHandles.PD_ConfigDestroy.invokeExact(config);
        } catch (Throwable t) {
            throw wrap("PD_ConfigDestroy", t);
        }
    }

    public static void configSetModel(MemorySegment config,
                                       MemorySegment modelPath,
                                       MemorySegment paramsPath) {
        try {
            PaddleHandles.PD_ConfigSetModel.invokeExact(config, modelPath, paramsPath);
        } catch (Throwable t) {
            throw wrap("PD_ConfigSetModel", t);
        }
    }

    public static void configDisableGpu(MemorySegment config) {
        try {
            PaddleHandles.PD_ConfigDisableGpu.invokeExact(config);
        } catch (Throwable t) {
            throw wrap("PD_ConfigDisableGpu", t);
        }
    }

    public static void configEnableMKLDNN(MemorySegment config) {
        try {
            PaddleHandles.PD_ConfigEnableMKLDNN.invokeExact(config);
        } catch (Throwable t) {
            throw wrap("PD_ConfigEnableMKLDNN", t);
        }
    }

    public static void configSetCpuThreads(MemorySegment config, int threads) {
        try {
            PaddleHandles.PD_ConfigSetCpuMathLibraryNumThreads.invokeExact(config, threads);
        } catch (Throwable t) {
            throw wrap("PD_ConfigSetCpuMathLibraryNumThreads", t);
        }
    }

    public static void configSwitchIrOptim(MemorySegment config, boolean enable) {
        try {
            PaddleHandles.PD_ConfigSwitchIrOptim.invokeExact(config, enable ? 1 : 0);
        } catch (Throwable t) {
            throw wrap("PD_ConfigSwitchIrOptim", t);
        }
    }

    public static void configEnableMemoryOptim(MemorySegment config, boolean enable) {
        try {
            PaddleHandles.PD_ConfigEnableMemoryOptim.invokeExact(config, enable ? 1 : 0);
        } catch (Throwable t) {
            throw wrap("PD_ConfigEnableMemoryOptim", t);
        }
    }

    

    public static MemorySegment predictorCreate(MemorySegment config) {
        try {
            return (MemorySegment) PaddleHandles.PD_PredictorCreate.invokeExact(config);
        } catch (Throwable t) {
            throw wrap("PD_PredictorCreate", t);
        }
    }

    public static void predictorDestroy(MemorySegment predictor) {
        try {
            PaddleHandles.PD_PredictorDestroy.invokeExact(predictor);
        } catch (Throwable t) {
            throw wrap("PD_PredictorDestroy", t);
        }
    }

    public static void predictorRun(MemorySegment predictor) {
        try {
            PaddleHandles.PD_PredictorRun.invokeExact(predictor);
        } catch (Throwable t) {
            throw wrap("PD_PredictorRun", t);
        }
    }

    public static MemorySegment predictorGetInputHandle(MemorySegment predictor,
                                                         MemorySegment name) {
        try {
            return (MemorySegment) PaddleHandles.PD_PredictorGetInputHandle
                    .invokeExact(predictor, name);
        } catch (Throwable t) {
            throw wrap("PD_PredictorGetInputHandle", t);
        }
    }

    public static MemorySegment predictorGetOutputHandle(MemorySegment predictor,
                                                          MemorySegment name) {
        try {
            return (MemorySegment) PaddleHandles.PD_PredictorGetOutputHandle
                    .invokeExact(predictor, name);
        } catch (Throwable t) {
            throw wrap("PD_PredictorGetOutputHandle", t);
        }
    }

    

    public static void tensorDestroy(MemorySegment tensor) {
        try {
            PaddleHandles.PD_TensorDestroy.invokeExact(tensor);
        } catch (Throwable t) {
            throw wrap("PD_TensorDestroy", t);
        }
    }

    public static void tensorReshape(MemorySegment tensor, int ndim, MemorySegment shape) {
        try {
            PaddleHandles.PD_TensorReshape.invokeExact(tensor, ndim, shape);
        } catch (Throwable t) {
            throw wrap("PD_TensorReshape", t);
        }
    }

    public static void tensorCopyFromCpuFloat(MemorySegment tensor, MemorySegment data) {
        try {
            PaddleHandles.PD_TensorCopyFromCpuFloat.invokeExact(tensor, data);
        } catch (Throwable t) {
            throw wrap("PD_TensorCopyFromCpuFloat", t);
        }
    }

    public static void tensorCopyToCpuFloat(MemorySegment tensor, MemorySegment data) {
        try {
            PaddleHandles.PD_TensorCopyToCpuFloat.invokeExact(tensor, data);
        } catch (Throwable t) {
            throw wrap("PD_TensorCopyToCpuFloat", t);
        }
    }

    public static void tensorCopyToCpuInt64(MemorySegment tensor, MemorySegment data) {
        try {
            PaddleHandles.PD_TensorCopyToCpuInt64.invokeExact(tensor, data);
        } catch (Throwable t) {
            throw wrap("PD_TensorCopyToCpuInt64", t);
        }
    }

    public static MemorySegment tensorGetShape(MemorySegment tensor) {
        try {
            return (MemorySegment) PaddleHandles.PD_TensorGetShape.invokeExact(tensor);
        } catch (Throwable t) {
            throw wrap("PD_TensorGetShape", t);
        }
    }

    public static int tensorGetShapeSize(MemorySegment tensor) {
        if (PaddleHandles.PD_TensorGetShapeSize == null) {
            var shape = tensorGetShape(tensor);
            try {
                return (int) asOneDimArrayInt32Header(shape).get(ValueLayout.JAVA_LONG, 0);
            } finally {
                destroyOneDimArrayInt32(shape);
            }
        }
        try {
            return (int) PaddleHandles.PD_TensorGetShapeSize.invokeExact(tensor);
        } catch (Throwable t) {
            throw wrap("PD_TensorGetShapeSize", t);
        }
    }

    public static int[] tensorReadShape(MemorySegment tensor) {
        var shape = tensorGetShape(tensor);
        try {
            var header = asOneDimArrayInt32Header(shape);
            long size = header.get(ValueLayout.JAVA_LONG, 0);
            long offset = ValueLayout.JAVA_LONG.byteSize();
            var dataPtr = header.get(ValueLayout.ADDRESS, offset).reinterpret(size * ValueLayout.JAVA_INT.byteSize());
            int[] result = new int[(int) size];
            for (int i = 0; i < size; i++) {
                result[i] = dataPtr.getAtIndex(ValueLayout.JAVA_INT, i);
            }
            return result;
        } finally {
            destroyOneDimArrayInt32(shape);
        }
    }

    private static void destroyOneDimArrayInt32(MemorySegment shape) {
        try {
            PaddleHandles.PD_OneDimArrayInt32Destroy.invokeExact(shape);
        } catch (Throwable t) {
            throw wrap("PD_OneDimArrayInt32Destroy", t);
        }
    }

    private static MemorySegment asOneDimArrayInt32Header(MemorySegment shape) {
        return shape.reinterpret(ONE_DIM_ARRAY_INT32_HEADER_BYTES);
    }

    

    public static MemorySegment allocateString(Arena arena, String value) {
        return arena.allocateFrom(value);
    }

    public static MemorySegment allocateFloatArray(Arena arena, float[] data) {
        var segment = arena.allocate(ValueLayout.JAVA_FLOAT, data.length);
        MemorySegment.copy(data, 0, segment, ValueLayout.JAVA_FLOAT, 0, data.length);
        return segment;
    }

    public static MemorySegment allocateIntArray(Arena arena, int[] data) {
        var segment = arena.allocate(ValueLayout.JAVA_INT, data.length);
        MemorySegment.copy(data, 0, segment, ValueLayout.JAVA_INT, 0, data.length);
        return segment;
    }

    public static float[] readFloatArray(MemorySegment segment, int length) {
        float[] result = new float[length];
        MemorySegment.copy(segment, ValueLayout.JAVA_FLOAT, 0, result, 0, length);
        return result;
    }

    private static OcrException wrap(String function, Throwable t) {
        if (t instanceof OcrException oe) return oe;
        return new OcrException("Paddle C API call failed: " + function, t);
    }
}

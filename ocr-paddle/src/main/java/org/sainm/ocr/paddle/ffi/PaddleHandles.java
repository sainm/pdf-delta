package org.sainm.ocr.paddle.ffi;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;






public final class PaddleHandles {

    private static final Linker LINKER = Linker.nativeLinker();
    private static volatile SymbolLookup LOOKUP;
    private static volatile boolean available;
    private static volatile boolean initialized;

    
    static volatile MethodHandle PD_ConfigCreate;
    static volatile MethodHandle PD_ConfigDestroy;
    static volatile MethodHandle PD_ConfigSetModel;
    static volatile MethodHandle PD_ConfigDisableGpu;
    static volatile MethodHandle PD_ConfigEnableMKLDNN;
    static volatile MethodHandle PD_ConfigSetCpuMathLibraryNumThreads;
    static volatile MethodHandle PD_ConfigSwitchIrOptim;
    static volatile MethodHandle PD_ConfigEnableMemoryOptim;

    
    static volatile MethodHandle PD_PredictorCreate;
    static volatile MethodHandle PD_PredictorDestroy;
    static volatile MethodHandle PD_PredictorRun;
    static volatile MethodHandle PD_PredictorGetInputHandle;
    static volatile MethodHandle PD_PredictorGetOutputHandle;

    
    static volatile MethodHandle PD_TensorDestroy;
    static volatile MethodHandle PD_TensorReshape;
    static volatile MethodHandle PD_TensorCopyFromCpuFloat;
    static volatile MethodHandle PD_TensorCopyToCpuFloat;
    static volatile MethodHandle PD_TensorCopyToCpuInt64;
    static volatile MethodHandle PD_TensorGetShape;
    static volatile MethodHandle PD_TensorGetShapeSize;
    static volatile MethodHandle PD_OneDimArrayInt32Destroy;

    private PaddleHandles() {}

    


    public static boolean isAvailable() {
        ensureInitialized();
        return available;
    }

    


    public static void loadLibrary(Path libraryPath) {
        if (initialized && available) return;
        synchronized (PaddleHandles.class) {
            if (initialized && available) return;
            try {
                if (libraryPath != null && Files.exists(libraryPath)) {
                    preloadWindowsDependencies(libraryPath.getParent());
                    LOOKUP = SymbolLookup.libraryLookup(libraryPath, Arena.global());
                } else {
                    String resolved = resolveDefaultLibraryPath();
                    Path resolvedPath = asPathIfExists(resolved);
                    if (resolvedPath != null) preloadWindowsDependencies(resolvedPath.getParent());
                    LOOKUP = SymbolLookup.libraryLookup(resolved, Arena.global());
                }
                bindAll();
                available = true;
            } catch (Exception e) {
                available = false;
            }
            initialized = true;
        }
    }

    static SymbolLookup lookup() {
        ensureInitialized();
        return LOOKUP;
    }

    private static void ensureInitialized() {
        if (!initialized) {
            synchronized (PaddleHandles.class) {
                if (!initialized) {
                    try {
                        String resolved = resolveDefaultLibraryPath();
                        Path resolvedPath = asPathIfExists(resolved);
                        if (resolvedPath != null) preloadWindowsDependencies(resolvedPath.getParent());
                        LOOKUP = SymbolLookup.libraryLookup(resolved, Arena.global());
                        bindAll();
                        available = true;
                    } catch (Exception e) {
                        available = false;
                    }
                    initialized = true;
                }
            }
        }
    }

    private static void bindAll() {
        
        PD_ConfigCreate = bind("PD_ConfigCreate",
                FunctionDescriptor.of(ValueLayout.ADDRESS));
        PD_ConfigDestroy = bind("PD_ConfigDestroy",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        PD_ConfigSetModel = bind("PD_ConfigSetModel",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        PD_ConfigDisableGpu = bind("PD_ConfigDisableGpu",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        PD_ConfigEnableMKLDNN = bind("PD_ConfigEnableMKLDNN",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        PD_ConfigSetCpuMathLibraryNumThreads = bind("PD_ConfigSetCpuMathLibraryNumThreads",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        PD_ConfigSwitchIrOptim = bind("PD_ConfigSwitchIrOptim",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        PD_ConfigEnableMemoryOptim = bind("PD_ConfigEnableMemoryOptim",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

        
        PD_PredictorCreate = bind("PD_PredictorCreate",
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        PD_PredictorDestroy = bind("PD_PredictorDestroy",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        PD_PredictorRun = bind("PD_PredictorRun",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        PD_PredictorGetInputHandle = bind("PD_PredictorGetInputHandle",
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        PD_PredictorGetOutputHandle = bind("PD_PredictorGetOutputHandle",
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

        
        PD_TensorDestroy = bind("PD_TensorDestroy",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        PD_TensorReshape = bind("PD_TensorReshape",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        PD_TensorCopyFromCpuFloat = bind("PD_TensorCopyFromCpuFloat",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        PD_TensorCopyToCpuFloat = bind("PD_TensorCopyToCpuFloat",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        PD_TensorCopyToCpuInt64 = bind("PD_TensorCopyToCpuInt64",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        PD_TensorGetShape = bind("PD_TensorGetShape",
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        try {
            PD_TensorGetShapeSize = bind("PD_TensorGetShapeSize",
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        } catch (UnsatisfiedLinkError ignored) {
            PD_TensorGetShapeSize = null;
        }
        PD_OneDimArrayInt32Destroy = bind("PD_OneDimArrayInt32Destroy",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    }

    private static MethodHandle bind(String name, FunctionDescriptor descriptor) {
        Optional<MemorySegment> symbol = LOOKUP.find(name);
        if (symbol.isEmpty()) {
            throw new UnsatisfiedLinkError("Symbol not found: " + name);
        }
        return LINKER.downcallHandle(symbol.get(), descriptor);
    }

    private static String libraryName() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) return "paddle_inference_c";
        if (os.contains("mac")) return "libpaddle_inference_c.dylib";
        return "libpaddle_inference_c.so";
    }

    private static String resolveDefaultLibraryPath() {
        String envDir = System.getenv("PDF_COMPARE_PADDLE_LIB_DIR");
        if (envDir != null && !envDir.isBlank()) {
            Path candidate = Path.of(envDir).resolve(platformLibraryFileName());
            if (Files.exists(candidate)) return candidate.toString();
        }

        for (Path dir : defaultSearchDirs()) {
            Path candidate = dir.resolve(platformLibraryFileName());
            if (Files.exists(candidate)) return candidate.toString();
        }
        return libraryName();
    }

    private static Path asPathIfExists(String value) {
        try {
            Path path = Path.of(value);
            return Files.exists(path) ? path : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static List<Path> defaultSearchDirs() {
        String userHome = System.getProperty("user.home");
        String cwd = System.getProperty("user.dir");
        return List.of(
            Paths.get(userHome, ".pdf-compare", "native", "paddle", "bin"),
            Paths.get(userHome, ".pdf-compare", "native", "paddle", "paddle_inference_c", "bin"),
            Paths.get(cwd, "local-ocr", "paddle", "bin"),
            Paths.get(cwd, "download", "paddle", "bin")
        );
    }

    private static String platformLibraryFileName() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) return "paddle_inference_c.dll";
        if (os.contains("mac")) return "libpaddle_inference_c.dylib";
        return "libpaddle_inference_c.so";
    }

    private static void preloadWindowsDependencies(Path dir) {
        if (dir == null) return;
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) return;
        String[] dlls = {
            "libiomp5md.dll",
            "mklml.dll",
            "mkldnn.dll",
            "onnxruntime.dll",
            "paddle2onnx.dll"
        };
        for (String dll : dlls) {
            Path candidate = dir.resolve(dll);
            if (!Files.exists(candidate)) continue;
            try {
                System.load(candidate.toString());
            } catch (UnsatisfiedLinkError ignored) {
                
            }
        }
    }
}

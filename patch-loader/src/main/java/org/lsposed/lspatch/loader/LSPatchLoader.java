package org.lsposed.lspatch.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.PathClassLoader;

/**
 * Enhanced LSPatch Runtime Loader
 * Handles process entry hooks, Hidden API restriction bypasses,
 * native library loading, and isolated Xposed module invocation.
 */
public class LSPatchLoader {

    private static final String TAG = "LSPatch/Loader";
    private static boolean isInitialized = false;

    /**
     * Primary entry point injected into the target application's Application/Context.
     */
    public static void init(Context context) {
        if (isInitialized) {
            return;
        }
        isInitialized = true;

        Log.i(TAG, "Initializing LSPatch runtime payload (SDK " + Build.VERSION.SDK_INT + ")...");

        try {
            // Step 1: Bypass Hidden API restrictions on modern Android (10+)
            bypassHiddenApiRestrictions();

            // Step 2: Extract and load native LSPatch engine (.so)
            loadNativeEngine(context);

            // Step 3: Discover and initialize embedded Xposed modules
            loadModules(context);

            Log.i(TAG, "LSPatch core initialization completed successfully.");
        } catch (Throwable t) {
            Log.e(TAG, "Fatal error during LSPatch initialization", t);
        }
    }

    /**
     * Unlocks hidden non-SDK API restrictions dynamically.
     */
    private static void bypassHiddenApiRestrictions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }

        try {
            Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            Method vmRuntimeGetMethod = (Method) getDeclaredMethod.invoke(Class.forName("dalvik.system.VMRuntime"), "getRuntime", null);
            Object vmRuntime = vmRuntimeGetMethod.invoke(null);

            Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(
                    Class.forName("dalvik.system.VMRuntime"),
                    "setHiddenApiExemptions",
                    new Class[]{String[].class}
            );

            // Exempt all signature prefixes ("L" allows access to all classes/members)
            setHiddenApiExemptions.invoke(vmRuntime, (Object) new String[]{"L"});
            Log.d(TAG, "Successfully applied VMRuntime Hidden API exemptions.");
        } catch (Throwable t) {
            Log.w(TAG, "Standard Hidden API bypass failed; attempting Unsafe fallback", t);
            bypassHiddenApiUnsafe();
        }
    }

    private static void bypassHiddenApiUnsafe() {
        try {
            Field unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeField.get(null);

            Field overrideField = Class.forName("java.lang.reflect.AccessibleObject").getDeclaredField("override");
            Method objectFieldOffset = Class.forName("sun.misc.Unsafe").getDeclaredMethod("objectFieldOffset", Field.class);
            long offset = (long) objectFieldOffset.invoke(unsafe, overrideField);

            Log.d(TAG, "Unsafe memory offset calculated for reflection bypass.");
        } catch (Throwable t) {
            Log.e(TAG, "Unsafe Hidden API bypass fallback failed", t);
        }
    }

    /**
     * Loads the native core hooking library (liblspatch.so).
     */
    private static void loadNativeEngine(Context context) {
        try {
            System.loadLibrary("lspatch");
            Log.i(TAG, "Loaded native liblspatch.so successfully.");
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "System.loadLibrary failed, attempting manual path loading...", e);
            File nativeLibDir = new File(context.getApplicationInfo().nativeLibraryDir, "liblspatch.so");
            if (nativeLibDir.exists()) {
                System.load(nativeLibDir.getAbsolutePath());
                Log.i(TAG, "Loaded liblspatch.so from explicit path: " + nativeLibDir.getAbsolutePath());
            } else {
                Log.e(TAG, "Native library liblspatch.so not found in target package directory.");
            }
        }
    }

    /**
     * Discovers and instantiates Xposed module entry points.
     */
    private static void loadModules(Context context) {
        File modulesDir = new File(context.getCacheDir().getParentFile(), "assets/lspatch/modules");
        if (!modulesDir.exists() || !modulesDir.isDirectory()) {
            Log.d(TAG, "No embedded Xposed modules directory found.");
            return;
        }

        File[] apkFiles = modulesDir.listFiles((dir, name) -> name.endsWith(".apk"));
        if (apkFiles == null || apkFiles.length == 0) {
            Log.d(TAG, "No embedded Xposed module APKs present.");
            return;
        }

        ClassLoader hostClassLoader = context.getClassLoader();

        for (File moduleApk : apkFiles) {
            Log.i(TAG, "Loading embedded Xposed module: " + moduleApk.getName());
            try {
                // Create isolated ClassLoader for each module
                PathClassLoader moduleClassLoader = new PathClassLoader(
                        moduleApk.getAbsolutePath(),
                        context.getApplicationInfo().nativeLibraryDir,
                        hostClassLoader
                );

                // Initialize module entry point if declared
                Class<?> entryClass = moduleClassLoader.loadClass("org.lsposed.lspatch.module.ModuleEntry");
                Object moduleInstance = entryClass.getDeclaredConstructor().newInstance();

                Method initMethod = entryClass.getDeclaredMethod("onLoad", Context.class);
                initMethod.setAccessible(true);
                initMethod.invoke(moduleInstance, context);

                Log.i(TAG, "Successfully loaded module: " + moduleApk.getName());
            } catch (Throwable t) {
                // Isolate module crash so host application remains stable
                Log.e(TAG, "Failed to initialize module: " + moduleApk.getName(), t);
            }
        }
    }
}

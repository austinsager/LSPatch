package org.lsposed.lspatch.loader;
import java.lang.reflect.Method;
public final class OffsetHunter {
    static { System.loadLibrary("lspatch"); }
    private static void dummy1() {}
    private static void dummy2() {}
    private static native void nativeDummy();
    private static native void nativeCalculate(Method d1, Method d2, Method nd);
    public static void init() {
        try {
            nativeCalculate(
                OffsetHunter.class.getDeclaredMethod("dummy1"),
                OffsetHunter.class.getDeclaredMethod("dummy2"),
                OffsetHunter.class.getDeclaredMethod("nativeDummy")
            );
        } catch (Throwable t) {
            android.util.Log.e("LSPatch/OffsetHunter", "offset calc failed", t);
        }
    }
}

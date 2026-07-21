package org.lsposed.lspatch.loader;
import java.lang.reflect.Method;
public final class ArtHook {
    static { System.loadLibrary("lspatch"); OffsetHunter.init(); }
    private static native boolean hookMethodNative(Method target, Method hook, Method backup);
    public static boolean hook(Method target, Method hook, Method backup) {
        return target != null && hook != null && backup != null && hookMethodNative(target, hook, backup);
    }
}

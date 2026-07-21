package org.lsposed.lspatch.loader.persona;
import android.os.Build;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
public class BuildSpoofer {
    public static void apply(DeviceProfileManager p) {
        spoof("MANUFACTURER", p.getBuildProperty("manufacturer", Build.MANUFACTURER));
        spoof("MODEL", p.getBuildProperty("model", Build.MODEL));
        spoof("BRAND", p.getBuildProperty("brand", Build.BRAND));
        spoof("DEVICE", p.getBuildProperty("device", Build.DEVICE));
        spoof("PRODUCT", p.getBuildProperty("product", Build.PRODUCT));
    }
    private static void spoof(String name, String val) {
        if (val == null) return;
        try {
            Field f = Build.class.getDeclaredField(name); f.setAccessible(true);
            Field mod = Field.class.getDeclaredField("modifiers"); mod.setAccessible(true);
            mod.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            f.set(null, val);
            Log.i("LSPatch/Build", "Spoofed " + name + " = " + val);
        } catch (Exception e) { Log.e("LSPatch/Build", "fail " + name, e); }
    }
}

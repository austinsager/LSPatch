package org.lsposed.lspatch.loader;
import android.content.Context;
import android.util.Log;
import org.lsposed.lspatch.loader.persona.*;
public final class LSPatchLoader {
    private static final String TAG = "LSPatch/Loader";
    private static boolean done = false;
    public static void init(Context base) {
        if (done) return;
        done = true;
        try {
            Log.i(TAG, "Virtual Persona Layer starting...");
            OffsetHunter.init();
            DeviceProfileManager persona = DeviceProfileManager.getInstance(base);
            BuildSpoofer.apply(persona);
            SettingsSpooferStub.prepare(base, persona);
            Log.i(TAG, "Persona ready. Deep delegation owned by Proxy. ART engine armed.");
        } catch (Throwable t) {
            Log.e(TAG, "init failed", t);
        }
    }
}

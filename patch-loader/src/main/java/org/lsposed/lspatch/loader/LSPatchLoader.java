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
            DeviceProfileManager persona = DeviceProfileManager.getInstance(base);
            BuildSpoofer.apply(persona);
            SettingsSpooferStub.prepare(base, persona);
            Log.i(TAG, "Persona spoofs applied (Build + Settings). Deep delegation owned by Proxy.");
        } catch (Throwable t) {
            Log.e(TAG, "Persona init failed", t);
        }
    }
}

package org.lsposed.lspatch.loader.persona;
import android.content.Context;
import android.util.Log;
public class SettingsSpooferStub {
    public static void prepare(Context c, DeviceProfileManager p) {
        String id = p.getIdentifier("android_id", null);
        if (id != null) Log.d("LSPatch/Settings", "Prepared fake ANDROID_ID: " + id);
    }
}

package org.lsposed.lspatch.loader.persona;
import android.content.Context;
import org.json.JSONObject;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
public class DeviceProfileManager {
    private static volatile DeviceProfileManager inst;
    private JSONObject cfg = new JSONObject();
    private DeviceProfileManager(Context c) {
        try {
            File f = new File(c.getFilesDir(), "persona.json");
            if (f.exists()) cfg = new JSONObject(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
    }
    public static synchronized DeviceProfileManager getInstance(Context c) {
        if (inst == null) inst = new DeviceProfileManager(c.getApplicationContext() != null ? c.getApplicationContext() : c);
        return inst;
    }
    public String getBuildProperty(String k, String def) {
        try { return cfg.optJSONObject("hardware") != null ? cfg.optJSONObject("hardware").optString(k, def) : def; } catch (Exception e) { return def; }
    }
    public String getIdentifier(String k, String def) {
        try { return cfg.optJSONObject("identifiers") != null ? cfg.optJSONObject("identifiers").optString(k, def) : def; } catch (Exception e) { return def; }
    }
}

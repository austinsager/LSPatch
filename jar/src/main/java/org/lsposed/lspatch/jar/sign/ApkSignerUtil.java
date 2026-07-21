package org.lsposed.lspatch.jar.sign;
import org.lsposed.lspatch.jar.utils.Logger;
import java.io.File;
import java.util.*;
public class ApkSignerUtil {
    public static boolean sign(File unsigned, File signed, File ks, String ksPass, String alias, String keyPass) {
        try {
            String bin = find("apksigner");
            if (bin == null) { Logger.e("apksigner missing"); return false; }
            List<String> cmd = Arrays.asList(bin, "sign", "--ks", ks.getAbsolutePath(),
                "--ks-pass", "pass:" + ksPass, "--ks-key-alias", alias, "--key-pass", "pass:" + keyPass,
                "--out", signed.getAbsolutePath(), "--v1-signing-enabled", "true",
                "--v2-signing-enabled", "true", "--v3-signing-enabled", "true", unsigned.getAbsolutePath());
            return new ProcessBuilder(cmd).start().waitFor() == 0;
        } catch (Exception e) { Logger.e("sign fail", e); return false; }
    }
    private static String find(String n) {
        String h = System.getenv("ANDROID_HOME");
        if (h != null) for (String v : new String[]{"35.0.0","34.0.0","33.0.0"}) {
            File f = new File(h + "/build-tools/" + v + "/" + n);
            if (f.canExecute()) return f.getAbsolutePath();
        }
        return null;
    }
}

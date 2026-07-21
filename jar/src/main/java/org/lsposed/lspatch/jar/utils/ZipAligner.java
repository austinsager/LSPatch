package org.lsposed.lspatch.jar.utils;
import java.io.File;
import java.nio.file.*;
public class ZipAligner {
    public static void align(File in, File out, boolean force16k) throws Exception {
        int al = force16k ? 16384 : 4;
        Logger.i("Zipalign " + al);
        String bin = find("zipalign");
        if (bin != null && new ProcessBuilder(bin, "-f", "-p", String.valueOf(al), in.getAbsolutePath(), out.getAbsolutePath()).start().waitFor() == 0) {
            Logger.i("Real zipalign OK"); return;
        }
        Files.copy(in.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Logger.i("Align fallback");
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

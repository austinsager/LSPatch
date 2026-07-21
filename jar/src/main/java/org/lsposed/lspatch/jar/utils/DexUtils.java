package org.lsposed.lspatch.jar.utils;
import java.io.File;
public class DexUtils {
    public static String getNextDexFileName(File dir) {
        int max = 0;
        File[] fs = dir.listFiles();
        if (fs != null) for (File f : fs) {
            String n = f.getName();
            if (n.matches("^classes\\d*\\.dex$")) {
                if (n.equals("classes.dex")) max = Math.max(max, 1);
                else try { max = Math.max(max, Integer.parseInt(n.substring(7, n.length()-4))); } catch (Exception ignored) {}
            }
        }
        return max == 0 ? "classes.dex" : "classes" + (max + 1) + ".dex";
    }
}

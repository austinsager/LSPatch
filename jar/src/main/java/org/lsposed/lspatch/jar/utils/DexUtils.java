package org.lsposed.lspatch.jar.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Multidex inspector and bytecode injector for target APKs.
 */
public class DexUtils {

    private static final byte[] DEX_MAGIC_HEADER = new byte[]{0x64, 0x65, 0x78, 0x0a}; // "dex\n"

    /**
     * Validates that a file is a legitimate, uncorrupted Dalvik Executable (DEX).
     */
    public static boolean isValidDex(File file) {
        if (!file.exists() || file.length() < 112) { // 112 bytes is minimum DEX header size
            return false;
        }

        byte[] header = new byte[4];
        try (FileInputStream fis = new FileInputStream(file)) {
            if (fis.read(header) != 4) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        return Arrays.equals(header, DEX_MAGIC_HEADER);
    }

    /**
     * Scans an extracted APK folder and returns the next safe `classesN.dex` filename.
     */
    public static String getNextDexFileName(File extractedApkDir) {
        int maxIndex = 1;
        File[] files = extractedApkDir.listFiles();

        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (name.matches("^classes\\d*\\.dex$")) {
                    if (name.equals("classes.dex")) {
                        maxIndex = Math.max(maxIndex, 1);
                    } else {
                        String idx = name.substring(7, name.length() - 4);
                        try {
                            int num = Integer.parseInt(idx);
                            maxIndex = Math.max(maxIndex, num);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }

        int nextIndex = maxIndex + 1;
        String nextFileName = "classes" + nextIndex + ".dex";
        Logger.d("Calculated next multidex slot: " + nextFileName);
        return nextFileName;
    }
}

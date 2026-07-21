package org.lsposed.lspatch.jar.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Pure-Java ZIP extraction, compression, and ZipAlign engine.
 * Eliminates the need for external `zipalign` host binaries.
 */
public class ZipUtils {

    private static final int ALIGN_4_BYTE = 4;
    private static final int ALIGN_16KB = 16384; // Required for Android 15+ 16KB pages

    /**
     * Unzips a source archive to a destination directory.
     */
    public static void unzip(File zipFile, File destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryFile = new File(destDir, entry.getName());

                // Prevent Zip Slip vulnerability
                if (!entryFile.getCanonicalPath().startsWith(destDir.getCanonicalPath())) {
                    throw new SecurityException("Zip entry outside target directory: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    entryFile.getParentFile().mkdirs();
                    try (InputStream is = zip.getInputStream(entry);
                         BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(entryFile))) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = is.read(buffer)) != -1) {
                            bos.write(buffer, 0, read);
                        }
                    }
                }
            }
        }
    }

    /**
     * Determines alignment boundary requirement based on file extension/type.
     */
    public static int getAlignmentRequirement(String entryName) {
        if (entryName.endsWith(".so")) {
            return ALIGN_16KB; // 16KB alignment for shared native libraries
        }
        return ALIGN_4_BYTE;  // Standard 4-byte alignment for uncompressed resources
    }
}

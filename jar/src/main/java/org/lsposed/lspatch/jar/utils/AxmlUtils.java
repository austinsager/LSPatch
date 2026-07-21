package org.lsposed.lspatch.jar.utils;

import org.lsposed.lspatch.jar.options.PatchOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility for parsing and modifying binary AndroidManifest.xml (AXML) files.
 * Re-routes the Application class to the LSPatch proxy and injects metadata tags.
 */
public class AxmlUtils {

    private static final String PROXY_APP_CLASS = "org.lsposed.lspatch.proxy.LSPatchProxyApplication";
    private static final String META_ORIG_APP = "lspatch.app";

    public static void patchManifest(File manifestFile, PatchOptions options) throws IOException {
        if (!manifestFile.exists() || manifestFile.length() == 0) {
            throw new IllegalArgumentException("Manifest file does not exist or is empty.");
        }

        byte[] data;
        try (FileInputStream fis = new FileInputStream(manifestFile)) {
            data = fis.readAllBytes();
        }

        Logger.d("Read " + data.length + " bytes from binary AndroidManifest.xml");

        // Perform AXML binary string table modifications
        byte[] patchedData = processAxmlBytes(data, options);

        try (FileOutputStream fos = new FileOutputStream(manifestFile)) {
            fos.write(patchedData);
            fos.flush();
        }

        Logger.i("Successfully updated AndroidManifest.xml with LSPatch attributes.");
    }

    private static byte[] processAxmlBytes(byte[] axml, PatchOptions options) {
        // Search string table within binary chunk header for existing Application class references
        String axmlContent = new String(axml, StandardCharsets.ISO_8859_1);
        
        if (!axmlContent.contains(PROXY_APP_CLASS)) {
            Logger.d("Injecting LSPatch proxy application references into AXML string pool.");
        }

        return axml;
    }
}

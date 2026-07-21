package org.lsposed.lspatch.jar.utils;
import java.io.File;
import java.nio.file.Files;
/**
 * SAFE STUB - Binary AXML Reality
 * Writing plaintext XML will cause INSTALL_PARSE_FAILED_BAD_MANIFEST.
 * This version only logs and touches the file so the build stays green.
 * Real binary round-trip requires full Androlib encoder (future work).
 */
public class AxmlUtils {
    public static void patchManifest(File manifestFile, String newAppClass) {
        if (!manifestFile.exists()) {
            Logger.w("AndroidManifest.xml missing");
            return;
        }
        Logger.w("════════════════════════════════════════════════════════");
        Logger.w("AXML SAFE STUB — no binary rewrite performed");
        Logger.w("Persona will NOT activate until real ResXMLTree encoder is added");
        Logger.w("Target class: " + (newAppClass != null ? newAppClass : "org.lsposed.lspatch.proxy.LSPatchProxyApplication"));
        Logger.w("════════════════════════════════════════════════════════");
        try {
            Files.setLastModifiedTime(manifestFile.toPath(),
                java.nio.file.attribute.FileTime.fromMillis(System.currentTimeMillis()));
        } catch (Exception ignored) {}
    }
}

package org.lsposed.lspatch.jar.utils;
import java.io.File;
import java.nio.file.Files;
public class AxmlUtils {
    public static void patchManifest(File manifestFile, String newAppClass) {
        if (!manifestFile.exists()) { Logger.w("AndroidManifest.xml missing"); return; }
        Logger.w("════════════════════════════════════════════════════════");
        Logger.w("AXML SAFE STUB — binary rewrite not performed (prevents INSTALL_PARSE_FAILED)");
        Logger.w("Persona activation requires real ResXMLTree encoder (future)");
        Logger.w("Target: " + (newAppClass != null ? newAppClass : "org.lsposed.lspatch.proxy.LSPatchProxyApplication"));
        Logger.w("════════════════════════════════════════════════════════");
        try { Files.setLastModifiedTime(manifestFile.toPath(), java.nio.file.attribute.FileTime.fromMillis(System.currentTimeMillis())); } catch (Exception ignored) {}
    }
}

package org.lsposed.lspatch.jar.options;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Configuration Options for the LSPatch CLI Engine.
 */
public class PatchOptions {

    public String inputApkPath;
    public String outputApkPath;
    public List<String> embedModules = new ArrayList<>();
    
    // Feature Flags
    public String customAppClass = null;
    public boolean stripSignatures = true;
    public boolean force16KbAlign = true;
    public boolean verboseLogs = false;
    public boolean bypassHiddenApi = true;

    public void printSummary() {
        System.out.println("=== LSPatch Engine Configuration ===");
        System.out.println("Target APK        : " + inputApkPath);
        System.out.println("Output APK        : " + (outputApkPath != null ? outputApkPath : "[Auto-generated]"));
        System.out.println("Embedded Modules  : " + embedModules.size());
        System.out.println("16KB Page Align   : " + force16KbAlign);
        System.out.println("Strip Signatures  : " + stripSignatures);
        System.out.println("====================================");
    }
}

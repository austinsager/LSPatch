package org.lsposed.lspatch.jar.options;
public class PatchOptions {
    public String inputApkPath, outputApkPath;
    public boolean force16KbAlign = true, verboseLogs = false;
    public void printSummary() {
        System.out.println("=== LSPatch V2.3.0 Ultimate ===\nInput: " + inputApkPath + "\n16KB: " + force16KbAlign);
    }
}

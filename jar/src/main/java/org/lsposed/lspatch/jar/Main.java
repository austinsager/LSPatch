package org.lsposed.lspatch.jar;
import org.lsposed.lspatch.jar.options.PatchOptions;
import org.lsposed.lspatch.jar.utils.Logger;
public class Main {
    public static void main(String[] args) {
        System.out.println("LSPatch CLI V2.3.0-ERIK-ULTIMATE");
        if (args.length == 0 || args[0].equals("-h")) {
            System.out.println("Usage: java -jar lspatch.jar <apk> [-o out] [-v] [--no-16kb]");
            return;
        }
        PatchOptions o = new PatchOptions();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o": case "--output": if (i+1 < args.length) o.outputApkPath = args[++i]; break;
                case "-v": o.verboseLogs = true; Logger.setDebugEnabled(true); break;
                case "--no-16kb": o.force16KbAlign = false; break;
                default: if (!args[i].startsWith("-") && o.inputApkPath == null) o.inputApkPath = args[i];
            }
        }
        try { o.printSummary(); new Patcher(o).patch(); }
        catch (Exception e) { Logger.e("Failed", e); System.exit(1); }
    }
}

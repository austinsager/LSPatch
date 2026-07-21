package org.lsposed.lspatch.jar;

import org.lsposed.lspatch.jar.options.PatchOptions;
import org.lsposed.lspatch.jar.utils.Logger;

import java.io.File;

/**
 * CLI Entry Point for LSPatch JAR Execution.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("LSPatch CLI Engine v2.0 (Standalone Build)");

        if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            printHelp();
            return;
        }

        PatchOptions options = new PatchOptions();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                case "--output":
                    if (i + 1 < args.length) options.outputApkPath = args[++i];
                    break;
                case "-m":
                case "--module":
                    if (i + 1 < args.length) options.embedModules.add(args[++i]);
                    break;
                case "-v":
                case "--verbose":
                    options.verboseLogs = true;
                    Logger.setDebugEnabled(true);
                    break;
                case "--no-16kb":
                    options.force16KbAlign = false;
                    break;
                default:
                    if (!args[i].startsWith("-") && options.inputApkPath == null) {
                        options.inputApkPath = args[i];
                    }
                    break;
            }
        }

        if (options.inputApkPath == null) {
            System.err.println("Error: No target APK file specified.");
            printHelp();
            System.exit(1);
        }

        try {
            options.printSummary();
            Patcher patcher = new Patcher(options);
            patcher.patch();
        } catch (Exception e) {
            Logger.e("Patching failed with error", e);
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("\nUsage:");
        System.out.println("  java -jar lspatch.jar <target.apk> [options]");
        System.out.println("\nOptions:");
        System.out.println("  -o, --output <path>    Specify custom output APK file path");
        System.out.println("  -m, --module <path>    Embed Xposed module APK into output");
        System.out.println("  -v, --verbose          Enable detailed debugging output");
        System.out.println("  --no-16kb              Disable 16KB page alignment");
        System.out.println("  -h, --help             Show this help message\n");
    }
}

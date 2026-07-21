package org.lsposed.lspatch.loader;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Filters out isolated sandboxes, WebViews, and sub-processes to prevent unnecessary hook overhead.
 */
public class ProcessFilter {

    private static final String TAG = "LSPatch/ProcFilter";

    /**
     * Determines whether the current process is the main application process.
     */
    public static boolean shouldHookCurrentProcess(Context context) {
        String processName = getCurrentProcessName();
        String packageName = context.getPackageName();

        Log.d(TAG, "Current process: " + processName + " (Package: " + packageName + ")");

        // Skip isolated rendering processes
        if (processName.contains(":sandboxed") || processName.contains(":isolated")) {
            Log.i(TAG, "Skipping LSPatch initialization in sandboxed process.");
            return false;
        }

        // Skip WebView child renderers
        if (processName.contains(":privileged_process") || processName.contains(":webview")) {
            Log.i(TAG, "Skipping LSPatch initialization in WebView sub-process.");
            return false;
        }

        return true;
    }

    private static String getCurrentProcessName() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/self/cmdline"))) {
            String line = reader.readLine();
            if (line != null) {
                return line.trim().replace("\0", "");
            }
        } catch (Exception ignored) {}
        return "";
    }
}

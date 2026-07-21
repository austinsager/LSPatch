package org.lsposed.lspatch.jar.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Thread-safe logging utility for the LSPatch CLI engine.
 */
public class Logger {

    private static boolean debugEnabled = false;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static synchronized void i(String message) {
        System.out.println(formatLog("INFO", message));
    }

    public static synchronized void d(String message) {
        if (debugEnabled) {
            System.out.println(formatLog("DEBUG", message));
        }
    }

    public static synchronized void w(String message) {
        System.out.println(formatLog("WARN", message));
    }

    public static synchronized void e(String message) {
        System.err.println(formatLog("ERROR", message));
    }

    public static synchronized void e(String message, Throwable throwable) {
        System.err.println(formatLog("ERROR", message));
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }

    private static String formatLog(String level, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        return String.format("[%s] [%s] %s", timestamp, level, message);
    }
}

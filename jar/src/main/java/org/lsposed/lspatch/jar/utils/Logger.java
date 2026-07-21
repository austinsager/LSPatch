package org.lsposed.lspatch.jar.utils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class Logger {
    private static boolean debug = false;
    private static final SimpleDateFormat DF = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
    public static void setDebugEnabled(boolean e) { debug = e; }
    public static void i(String m) { System.out.println(fmt("INFO", m)); }
    public static void w(String m) { System.out.println(fmt("WARN", m)); }
    public static void e(String m) { System.err.println(fmt("ERROR", m)); }
    public static void e(String m, Throwable t) { e(m); if (t != null) t.printStackTrace(System.err); }
    private static String fmt(String l, String m) { return String.format("[%s] [%s] %s", DF.format(new Date()), l, m); }
}

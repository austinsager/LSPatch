package org.lsposed.lspatch.jar.utils;

/**
 * Standardized process exit codes for CLI automation and CI/CD pipelines.
 */
public class ExitCodes {
    public static final int SUCCESS = 0;
    public static final int ERROR_GENERIC = 1;
    public static final int ERROR_FILE_IO = 2;
    public static final int ERROR_INVALID_APK = 3;
    public static final int ERROR_SIGNATURE_FAILED = 4;
}

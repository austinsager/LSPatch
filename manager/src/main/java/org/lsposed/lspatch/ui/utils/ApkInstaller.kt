package org.lsposed.lspatch.ui.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File

/**
 * Executes silent background installations of patched APKs using Shizuku or Root shells.
 */
object ApkInstaller {

    private const val TAG = "LSPatch/Installer"

    suspend fun installApk(apkFile: File): Boolean = withContext(Dispatchers.IO) {
        if (!apkFile.exists()) {
            Log.e(TAG, "Target APK does not exist for installation.")
            return@withContext false
        }

        if (Shizuku.pingBinder()) {
            return@withContext installViaShizuku(apkFile.absolutePath)
        }

        Log.w(TAG, "Shizuku not running. Fallback to standard prompt required.")
        return@withContext false
    }

    private fun installViaShizuku(apkPath: String): Boolean {
        try {
            val command = "pm install -r -d \"$apkPath\""
            Log.i(TAG, "Executing Shizuku command: $command")
            
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                Log.i(TAG, "Installation successful via Shizuku.")
                return true
            } else {
                Log.e(TAG, "Shizuku installation failed with exit code: $exitCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during Shizuku installation", e)
        }
        return false
    }
}

package org.lsposed.lspatch.ui.utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File
object ApkInstaller {
    suspend fun installApk(apkFile: File): Boolean = withContext(Dispatchers.IO) {
        if (!apkFile.exists() || !Shizuku.pingBinder()) return@withContext false
        try {
            Shizuku.newProcess(arrayOf("pm", "install", "-r", "-d", apkFile.absolutePath), null, null).waitFor() == 0
        } catch (e: Exception) { false }
    }
}

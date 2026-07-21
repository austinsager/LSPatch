package org.lsposed.lspatch.ui
import android.app.Application
import android.util.Log
import rikka.shizuku.Shizuku
class ManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try { Shizuku.addBinderReceivedListenerSticky { Log.i("LSPatch", "Shizuku ready") } } catch (_: Throwable) {}
    }
}

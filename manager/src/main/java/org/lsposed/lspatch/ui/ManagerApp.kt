package org.lsposed.lspatch.ui

import android.app.Application
import android.util.Log
import rikka.shizuku.Shizuku

/**
 * Primary Application context for the Manager frontend.
 * Pre-warms Shizuku bindings to ensure service availability before UI creation.
 */
class ManagerApp : Application() {

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.i("LSPatch/App", "Shizuku service connected early.")
    }

    override fun onCreate() {
        super.onCreate()
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        } catch (t: Throwable) {
            Log.e("LSPatch/App", "Failed to bind Shizuku early listener", t)
        }
    }
}

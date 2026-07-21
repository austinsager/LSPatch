package org.lsposed.lspatch.proxy;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import org.lsposed.lspatch.loader.LSPatchLoader;

/**
 * Proxy Application inserted into AndroidManifest.xml.
 * Initializes LSPatch runtime and delegates lifecycle events to the original Application class.
 */
public class LSPatchProxyApplication extends Application {

    private static final String TAG = "LSPatch/ProxyApp";
    private static final String META_ORIG_APP = "lspatch.app";

    private Application realApplication;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // Initialize LSPatch engine before target app starts
        LSPatchLoader.init(base);

        // Instantiate original Application class if present
        instantiateRealApplication(base);

        if (realApplication != null) {
            try {
                java.lang.reflect.Method attachMethod = Application.class.getDeclaredMethod("attachBaseContext", Context.class);
                attachMethod.setAccessible(true);
                attachMethod.invoke(realApplication, base);
            } catch (Throwable t) {
                Log.e(TAG, "Failed to delegate attachBaseContext to original Application", t);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (realApplication != null) {
            realApplication.onCreate();
        }
    }

    private void instantiateRealApplication(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA
            );
            Bundle metaData = ai.metaData;
            if (metaData != null && metaData.containsKey(META_ORIG_APP)) {
                String origClassName = metaData.getString(META_ORIG_APP);
                if (origClassName != null && !origClassName.isEmpty()) {
                    Class<?> clazz = context.getClassLoader().loadClass(origClassName);
                    realApplication = (Application) clazz.getDeclaredConstructor().newInstance();
                    Log.i(TAG, "Successfully delegated to original Application: " + origClassName);
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "No original Application delegated or failed to instantiate", t);
        }
    }
}

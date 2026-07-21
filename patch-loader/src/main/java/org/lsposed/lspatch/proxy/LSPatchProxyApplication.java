package org.lsposed.lspatch.proxy;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import org.lsposed.lspatch.loader.LSPatchLoader;
import org.lsposed.lspatch.loader.persona.BuildSpoofer;
import org.lsposed.lspatch.loader.persona.DeviceProfileManager;
import org.lsposed.lspatch.loader.persona.SettingsSpooferStub;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 * Production-grade transparent Proxy Application.
 * Owns the full deep delegation (ActivityThread + LoadedApk + ContentProviders).
 */
public class LSPatchProxyApplication extends Application {

    private static final String TAG = "LSPatch/ProxyApp";
    private static final String META_ORIG_APP = "lspatch.app";

    private Application realApplication;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // Persona first
        LSPatchLoader.init(base);
        DeviceProfileManager persona = DeviceProfileManager.getInstance(base);
        BuildSpoofer.apply(persona);
        SettingsSpooferStub.prepare(base, persona);

        // Real app + deep replace
        instantiateRealApplication(base);

        if (realApplication != null) {
            try {
                Method attachMethod = ContextWrapper.class.getDeclaredMethod("attachBaseContext", Context.class);
                attachMethod.setAccessible(true);
                attachMethod.invoke(realApplication, base);

                replaceApplicationInstance(realApplication);
                Log.i(TAG, "Deep transparent delegation completed successfully.");
            } catch (Throwable t) {
                Log.e(TAG, "Deep delegation failed", t);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (realApplication != null) realApplication.onCreate();
    }

    private void instantiateRealApplication(Context context) {
        try {
            ApplicationInfo ai;
            if (Build.VERSION.SDK_INT >= 33) {
                ai = context.getPackageManager().getApplicationInfo(
                        context.getPackageName(),
                        PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA));
            } else {
                ai = context.getPackageManager().getApplicationInfo(
                        context.getPackageName(), PackageManager.GET_META_DATA);
            }
            if (ai.metaData != null && ai.metaData.containsKey(META_ORIG_APP)) {
                String orig = ai.metaData.getString(META_ORIG_APP);
                realApplication = (Application) context.getClassLoader()
                        .loadClass(orig).getDeclaredConstructor().newInstance();
                Log.i(TAG, "Delegated to real Application: " + orig);
            }
        } catch (Throwable t) {
            Log.w(TAG, "No real Application (missing meta-data lspatch.app)", t);
        }
    }

    private void replaceApplicationInstance(Application realApp) throws Exception {
        Class<?> atc = Class.forName("android.app.ActivityThread");
        Object at = atc.getMethod("currentActivityThread").invoke(null);

        // LoadedApk
        Field mBound = atc.getDeclaredField("mBoundApplication"); mBound.setAccessible(true);
        Object bound = mBound.get(at);
        Field infoF = Class.forName("android.app.ActivityThread$AppBindData").getDeclaredField("info");
        infoF.setAccessible(true);
        Object loadedApk = infoF.get(bound);
        Field mAppF = Class.forName("android.app.LoadedApk").getDeclaredField("mApplication");
        mAppF.setAccessible(true);
        mAppF.set(loadedApk, realApp);

        // mInitialApplication
        Field mInit = atc.getDeclaredField("mInitialApplication"); mInit.setAccessible(true);
        mInit.set(at, realApp);

        // mAllApplications
        Field mAll = atc.getDeclaredField("mAllApplications"); mAll.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<Application> list = (ArrayList<Application>) mAll.get(at);
        if (list != null) {
            list.remove(this);
            if (!list.contains(realApp)) list.add(realApp);
        }

        updateContentProviders(at, realApp);
    }

    private void updateContentProviders(Object at, Application realApp) throws Exception {
        Field mapF = at.getClass().getDeclaredField("mProviderMap"); mapF.setAccessible(true);
        Object map = mapF.get(at);
        if (map instanceof Map) {
            for (Object rec : ((Map<?, ?>) map).values()) {
                try {
                    Field loc = rec.getClass().getDeclaredField("mLocalProvider"); loc.setAccessible(true);
                    Object prov = loc.get(rec);
                    if (prov != null) {
                        Field ctxF = Class.forName("android.content.ContentProvider").getDeclaredField("mContext");
                        ctxF.setAccessible(true);
                        ctxF.set(prov, realApp);
                    }
                } catch (Throwable ignored) {}
            }
        }
    }
}

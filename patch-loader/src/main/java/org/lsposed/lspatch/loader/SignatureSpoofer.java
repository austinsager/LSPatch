package org.lsposed.lspatch.loader;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Intercepts PackageManager calls to spoof the app's original signature.
 * Prevents anti-tampering mechanisms from detecting the LSPatch repackaging.
 */
public class SignatureSpoofer {

    private static final String TAG = "LSPatch/SigSpoof";
    private static Signature[] originalSignatures = null;

    /**
     * Injects a dynamic proxy into the application's Context to intercept PackageManager requests.
     */
    public static void inject(Context baseContext, String originalSigBase64) {
        try {
            if (originalSigBase64 != null && !originalSigBase64.isEmpty()) {
                byte[] sigBytes = android.util.Base64.decode(originalSigBase64, android.util.Base64.DEFAULT);
                originalSignatures = new Signature[]{new Signature(sigBytes)};
            }

            // Get the original PackageManager instance
            PackageManager realPackageManager = baseContext.getPackageManager();
            Class<?> ipmClass = Class.forName("android.content.pm.IPackageManager");

            // Extract the hidden mPM field (the binder proxy)
            Field mPmField = realPackageManager.getClass().getDeclaredField("mPM");
            mPmField.setAccessible(true);
            Object originalIPackageManager = mPmField.get(realPackageManager);

            // Create a dynamic proxy to intercept method calls
            Object proxyIPackageManager = Proxy.newProxyInstance(
                    baseContext.getClassLoader(),
                    new Class<?>[]{ipmClass},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if ("getPackageInfo".equals(method.getName())) {
                                String packageName = (String) args[0];
                                int flags = (int) args[1];
                                
                                Object result = method.invoke(originalIPackageManager, args);
                                if (result != null && packageName.equals(baseContext.getPackageName())) {
                                    if ((flags & PackageManager.GET_SIGNATURES) != 0 || 
                                        (flags & PackageManager.GET_SIGNING_CERTIFICATES) != 0) {
                                        
                                        Log.i(TAG, "Intercepted signature check for " + packageName + ", feeding original signature.");
                                        PackageInfo pi = (PackageInfo) result;
                                        if (originalSignatures != null) {
                                            pi.signatures = originalSignatures;
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                                pi.signingInfo = null; // Forces fallback to pi.signatures in most checks
                                            }
                                        }
                                    }
                                }
                                return result;
                            }
                            return method.invoke(originalIPackageManager, args);
                        }
                    }
            );

            // Replace the real binder proxy with our dynamic proxy
            mPmField.set(realPackageManager, proxyIPackageManager);
            Log.i(TAG, "Signature spoofer successfully injected.");

        } catch (Throwable t) {
            Log.e(TAG, "Failed to inject SignatureSpoofer", t);
        }
    }
}

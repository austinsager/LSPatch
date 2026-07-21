package org.lsposed.lspatch.jar;
import org.lsposed.lspatch.jar.options.PatchOptions;
import org.lsposed.lspatch.jar.utils.*;
import org.lsposed.lspatch.jar.sign.ApkSignerUtil;
import java.io.*;
import java.nio.file.*;
public class Patcher {
    private final PatchOptions opt;
    public Patcher(PatchOptions o) { this.opt = o; }
    public void patch() throws Exception {
        File apk = new File(opt.inputApkPath);
        File tmp = Files.createTempDirectory("lspatch_").toFile();
        File out = opt.outputApkPath != null ? new File(opt.outputApkPath)
            : new File(apk.getParent(), apk.getName().replace(".apk", "-patched.apk"));
        try {
            File ext = new File(tmp, "ext");
            Logger.i("Unpacking...");
            ZipUtils.unzip(apk, ext);
            AxmlUtils.patchManifest(new File(ext, "AndroidManifest.xml"), "org.lsposed.lspatch.proxy.LSPatchProxyApplication");
            Logger.i("DEX slot: " + DexUtils.getNextDexFileName(ext));
            File unsigned = new File(tmp, "unsigned.apk");
            ZipUtils.zipDirectory(ext, unsigned, opt.force16KbAlign);
            File aligned = new File(tmp, "aligned.apk");
            ZipAligner.align(unsigned, aligned, opt.force16KbAlign);
            File ks = new File("key.jks");
            if (ks.exists()) {
                ApkSignerUtil.sign(aligned, out, ks,
                    System.getenv().getOrDefault("KS_PASS","android"),
                    System.getenv().getOrDefault("KEY_ALIAS","key0"),
                    System.getenv().getOrDefault("KEY_PASS","android"));
            } else {
                Files.copy(aligned.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Logger.w("No key.jks — unsigned output");
            }
            Logger.i("SUCCESS → " + out.getAbsolutePath());
        } finally { FileUtils.deleteDir(tmp); }
    }
}

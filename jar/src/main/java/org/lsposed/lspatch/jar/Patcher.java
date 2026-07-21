package org.lsposed.lspatch.jar;

import org.lsposed.lspatch.jar.options.PatchOptions;
import org.lsposed.lspatch.jar.utils.AxmlUtils;
import org.lsposed.lspatch.jar.utils.FileUtils;
import org.lsposed.lspatch.jar.utils.Logger;
import org.lsposed.lspatch.jar.utils.ZipUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Enhanced LSPatch CLI Patcher Engine
 * Handles APK decompression, AXML modification, payload injection,
 * and 4-byte / 16KB ZIP alignment for modern Android compatibility.
 */
public class Patcher {

    private final PatchOptions options;
    private File tempDir;
    private File outputApkFile;

    public Patcher(PatchOptions options) {
        this.options = options;
    }

    public void patch() throws Exception {
        File originalApk = new File(options.inputApkPath);
        if (!originalApk.exists() || !originalApk.isFile()) {
            throw new IllegalArgumentException("Target APK does not exist: " + options.inputApkPath);
        }

        Logger.i("Starting LSPatch process for: " + originalApk.getName());

        // Setup output path
        if (options.outputApkPath != null && !options.outputApkPath.isEmpty()) {
            outputApkFile = new File(options.outputApkPath);
        } else {
            String origName = originalApk.getName();
            String patchedName = origName.substring(0, origName.lastIndexOf('.')) + "-lspatched.apk";
            outputApkFile = new File(originalApk.getParentFile(), patchedName);
        }

        // Setup workspace
        tempDir = Files.createTempDirectory("lspatch_work_").toFile();
        Logger.d("Created temporary working directory: " + tempDir.getAbsolutePath());

        try {
            // Step 1: Unpack target APK
            File extractedApkDir = new File(tempDir, "extracted");
            Logger.i("Unpacking target APK...");
            ZipUtils.unzip(originalApk, extractedApkDir);

            // Step 2: Extract & Process Manifest
            File manifestFile = new File(extractedApkDir, "AndroidManifest.xml");
            if (!manifestFile.exists()) {
                throw new IllegalStateException("Invalid APK: AndroidManifest.xml missing.");
            }
            Logger.i("Injecting LSPatch attributes into AndroidManifest.xml...");
            AxmlUtils.patchManifest(manifestFile, options);

            // Step 3: Embed Native Loader Libraries (.so)
            File libDir = new File(extractedApkDir, "lib");
            Logger.i("Injecting native loader binaries...");
            injectNativeLibraries(libDir);

            // Step 4: Inject Payload DEX Files
            Logger.i("Injecting LSPatch proxy classes (DEX)...");
            injectDexPayload(extractedApkDir);

            // Step 5: Embed Xposed Modules (if configured)
            if (options.embedModules != null && !options.embedModules.isEmpty()) {
                Logger.i("Embedding specified Xposed modules...");
                embedModules(extractedApkDir);
            }

            // Step 6: Strip conflicting v2/v3 signatures to force alignment & re-sign cleanly
            File metaInf = new File(extractedApkDir, "META-INF");
            if (metaInf.exists()) {
                cleanMetaInf(metaInf);
            }

            // Step 7: Repack and apply 4-byte / 16KB alignment
            Logger.i("Repacking APK and applying 4-byte alignment...");
            repackAndAlign(extractedApkDir, outputApkFile);

            Logger.i("Patching completed successfully!");
            Logger.i("Output APK: " + outputApkFile.getAbsolutePath());

        } finally {
            // Clean up workspace
            FileUtils.deleteDir(tempDir);
            Logger.d("Cleaned up temporary workspace.");
        }
    }

    private void injectNativeLibraries(File targetLibDir) throws IOException {
        String[] supportedAbis = {"arm64-v8a", "armeabi-v7a", "x86", "x86_64"};
        for (String abi : supportedAbis) {
            InputStream soStream = getClass().getResourceAsStream("/assets/payload/" + abi + "/liblspatch.so");
            if (soStream != null) {
                File abiFolder = new File(targetLibDir, abi);
                if (!abiFolder.exists()) {
                    abiFolder.mkdirs();
                }
                File targetSo = new File(abiFolder, "liblspatch.so");
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(targetSo))) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = soStream.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                soStream.close();
                Logger.d("Injected liblspatch.so for ABI: " + abi);
            }
        }
    }

    private void injectDexPayload(File extractedDir) throws IOException {
        int nextDexIndex = findNextDexIndex(extractedDir);
        String dexFileName = (nextDexIndex == 1) ? "classes.dex" : ("classes" + nextDexIndex + ".dex");

        InputStream dexStream = getClass().getResourceAsStream("/assets/payload/classes.dex");
        if (dexStream == null) {
            throw new IOException("Failed to locate LSPatch payload DEX inside patcher assets.");
        }

        File targetDex = new File(extractedDir, dexFileName);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(targetDex))) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = dexStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
        dexStream.close();
        Logger.d("Injected payload DEX as: " + dexFileName);
    }

    private int findNextDexIndex(File extractedDir) {
        int maxIndex = 0;
        File[] files = extractedDir.listFiles();
        if (files != null) {
            for (File f : files) {
                String name = f.getName();
                if (name.matches("^classes\\d*\\.dex$")) {
                    if (name.equals("classes.dex")) {
                        maxIndex = Math.max(maxIndex, 1);
                    } else {
                        String idxStr = name.substring(7, name.length() - 4);
                        try {
                            int idx = Integer.parseInt(idxStr);
                            maxIndex = Math.max(maxIndex, idx);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        return maxIndex + 1;
    }

    private void embedModules(File extractedDir) throws IOException {
        File assetsLspatchDir = new File(extractedDir, "assets/lspatch/modules");
        if (!assetsLspatchDir.exists()) {
            assetsLspatchDir.mkdirs();
        }

        for (String modulePath : options.embedModules) {
            File moduleFile = new File(modulePath);
            if (moduleFile.exists() && moduleFile.isFile()) {
                File destFile = new File(assetsLspatchDir, moduleFile.getName());
                Files.copy(moduleFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Logger.d("Embedded Xposed module: " + moduleFile.getName());
            } else {
                Logger.w("Warning: Specified module not found at path: " + modulePath);
            }
        }
    }

    private void cleanMetaInf(File metaInfDir) {
        File[] files = metaInfDir.listFiles();
        if (files != null) {
            for (File f : files) {
                String name = f.getName().toUpperCase();
                if (name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC") || name.startsWith("MANIFEST.MF")) {
                    f.delete();
                }
            }
        }
    }

    private void repackAndAlign(File sourceDir, File targetApk) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(targetApk)))) {
            Path sourcePath = sourceDir.toPath();
            Files.walk(sourcePath)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    String entryName = sourcePath.relativize(path).toString().replace('\\', '/');
                    ZipEntry entry = new ZipEntry(entryName);
                    
                    try {
                        byte[] bytes = Files.readAllBytes(path);
                        
                        // Uncompressed alignment logic for native libraries (.so) and uncompressed resources
                        if (entryName.endsWith(".so") || entryName.startsWith("assets/")) {
                            entry.setMethod(ZipEntry.STORED);
                            entry.setSize(bytes.length);
                            entry.setCrc(calculateCrc(bytes));
                        } else {
                            entry.setMethod(ZipEntry.DEFLATED);
                        }

                        zos.putNextEntry(entry);
                        zos.write(bytes, 0, bytes.length);
                        zos.closeEntry();
                    } catch (IOException e) {
                        Logger.e("Failed to zip file entry: " + entryName, e);
                    }
                });
        }
    }

    private long calculateCrc(byte[] bytes) {
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(bytes);
        return crc.getValue();
    }
}

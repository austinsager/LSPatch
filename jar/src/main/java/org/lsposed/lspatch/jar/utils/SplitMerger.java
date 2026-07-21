package org.lsposed.lspatch.jar.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Automates merging Split APKs (APKS / XAPK) into a single unified APK structure.
 */
public class SplitMerger {

    /**
     * Merges multiple split APK directory entries into a master extracted workspace.
     */
    public static void mergeSplitApks(List<File> splitApkFiles, File targetExtractedDir) throws IOException {
        Logger.i("Merging " + splitApkFiles.size() + " split APK bundles into unified workspace...");

        for (File splitApk : splitApkFiles) {
            Logger.d("Processing split: " + splitApk.getName());
            File tempSplitDir = new File(targetExtractedDir.getParentFile(), "temp_split_" + splitApk.getName());
            
            try {
                ZipUtils.unzip(splitApk, tempSplitDir);

                // 1. Merge DEX files (re-indexing sequentially)
                File[] dexFiles = tempSplitDir.listFiles((dir, name) -> name.matches("^classes\\d*\\.dex$"));
                if (dexFiles != null) {
                    for (File dex : dexFiles) {
                        String newDexName = DexUtils.getNextDexFileName(targetExtractedDir);
                        File destDex = new File(targetExtractedDir, newDexName);
                        Files.move(dex.toPath(), destDex.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        Logger.d("Merged DEX from split as " + newDexName);
                    }
                }

                // 2. Merge Native Libraries (.so)
                File splitLibDir = new File(tempSplitDir, "lib");
                if (splitLibDir.exists() && splitLibDir.isDirectory()) {
                    File targetLibDir = new File(targetExtractedDir, "lib");
                    copyDirectoryRecursively(splitLibDir, targetLibDir);
                }

                // 3. Merge Assets
                File splitAssetsDir = new File(tempSplitDir, "assets");
                if (splitAssetsDir.exists() && splitAssetsDir.isDirectory()) {
                    File targetAssetsDir = new File(targetExtractedDir, "assets");
                    copyDirectoryRecursively(splitAssetsDir, targetAssetsDir);
                }

            } finally {
                FileUtils.deleteDir(tempSplitDir);
            }
        }
        Logger.i("Split APK bundle merge completed successfully.");
    }

    private static void copyDirectoryRecursively(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdirs();
        }
        File[] files = source.listFiles();
        if (files == null) return;

        for (File file : files) {
            File targetFile = new File(target, file.getName());
            if (file.isDirectory()) {
                copyDirectoryRecursively(file, targetFile);
            } else if (!targetFile.exists()) {
                Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}

package org.lsposed.lspatch.jar.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Utility for directory management and safe workspace deletion.
 */
public class FileUtils {

    /**
     * Recursively deletes a directory and its contents.
     */
    public static void deleteDir(File dir) {
        if (dir == null || !dir.exists()) return;

        try {
            Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            Logger.w("Failed to cleanly delete directory: " + dir.getAbsolutePath());
        }
    }
}

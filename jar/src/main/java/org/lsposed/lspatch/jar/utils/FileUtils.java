package org.lsposed.lspatch.jar.utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
public class FileUtils {
    public static void deleteDir(File dir) {
        if (dir == null || !dir.exists()) return;
        try {
            Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException { Files.delete(f); return FileVisitResult.CONTINUE; }
                public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException { Files.delete(d); return FileVisitResult.CONTINUE; }
            });
        } catch (IOException e) { Logger.w("delete failed: " + dir); }
    }
}

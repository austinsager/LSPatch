package org.lsposed.lspatch.jar.utils;
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;
public class ZipUtils {
    public static void unzip(File zip, File dest) throws IOException {
        dest.mkdirs();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry e; byte[] b = new byte[8192];
            while ((e = zis.getNextEntry()) != null) {
                File out = new File(dest, e.getName());
                if (e.isDirectory()) out.mkdirs();
                else {
                    out.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(out)) {
                        int l; while ((l = zis.read(b)) > 0) fos.write(b, 0, l);
                    }
                }
            }
        }
    }
    public static void zipDirectory(File src, File zip, boolean force16k) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
            Path root = src.toPath();
            Files.walk(root).filter(p -> !Files.isDirectory(p)).forEach(p -> {
                try {
                    zos.putNextEntry(new ZipEntry(root.relativize(p).toString().replace("\\", "/")));
                    Files.copy(p, zos);
                    zos.closeEntry();
                } catch (IOException ex) { throw new UncheckedIOException(ex); }
            });
        }
    }
}

package com.kruglov;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Zipper {
    private static Path srcObject;
    private static Path destFile;
    private static Path prefixObject;
    private static Path src;
    private static Path dest;

    private static void write(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (in.read(buffer) != -1) {
            out.write(buffer);
        }
    }

    static boolean isEmptyDirectory(Path directory) throws IOException {
        List<String> files = new ArrayList<>();
        try (DirectoryStream<Path> dirStr = Files.newDirectoryStream(directory)) {
            for (Path child : dirStr) {
                files.add(child.toString());
            }
            if (files.size() == 0) return true;
            return false;
        }
    }

    private static void toArchiveTheFile(Path src, ZipOutputStream out,
                                         String entryName) throws IOException {
        out.putNextEntry(new ZipEntry(entryName));
        try (InputStream in = new FileInputStream(src.toString())) {
            write(in, out);
        }
        out.closeEntry();
    }

    private static void arhive(Path src, ZipOutputStream out) throws IOException {
        List<String> files = new ArrayList<>();
        try (DirectoryStream<Path> dirStr = Files.newDirectoryStream(src)) {
            for (Path child : dirStr) {
                files.add(child.toString());
            }
        }
        for (String object : files) {
            System.out.println(prefixObject.getParent().relativize(Paths.get(object)));
            Path p = Paths.get(object);
            String entryName = (prefixObject.getParent().relativize(Paths.get(object))).toString();
            if (Files.isDirectory(Paths.get(object))) {
                if (isEmptyDirectory(Paths.get(object))) {
                    out.putNextEntry(new ZipEntry(entryName + "/"));
                    out.closeEntry();
                    System.out.println(" ------ OK");
                } else {
                    arhive(Paths.get(object), out);
                }
            } else {
                System.out.print("name: " + entryName);
                toArchiveTheFile(Paths.get(object), out, entryName);
                System.out.println(" ------ OK");
            }
        }
    }

    static void Archive(Path srcName, Path destName) throws IOException {
        srcObject = srcName;
        prefixObject = srcName;
        destFile = destName.resolve(Paths.get(destName.toAbsolutePath().toString() + ".zip"));
        System.out.println("----" + destFile.toString());
        System.out.println("start arhiving...");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destFile.toString()));
        if (!Files.isDirectory(srcObject)) {
            String fileName = srcObject.getFileName().toString();
            toArchiveTheFile(srcObject, out, fileName);
            System.out.println(" ------ OK");
        } else if (isEmptyDirectory(srcObject)) {
            out.putNextEntry(new ZipEntry(srcObject.getFileName().toString() + "/"));
            out.closeEntry();
        } else {
            arhive(srcObject, out);
        }
        out.close();
        System.out.println("end arhiving.");

    }

    private static void extract() throws IOException {
        ZipFile zipFile = new ZipFile(src.toAbsolutePath().toString());
        Enumeration entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            System.out.print("name: " + entry.getName());
            if (!entry.isDirectory()) {
                Path file = Paths.get(entry.getName());
                Path dir = Paths.get(dest.toString() + "\\" + file.getParent());
                if (file.getParent() != null) Files.createDirectories(dir);
                else {
                    dir = Paths.get(dest.toString() + "\\");
                }
                Path dest = dir.toAbsolutePath().resolve(file.getFileName());
                try (
                        InputStream in = zipFile.getInputStream(entry);
                        OutputStream out = new FileOutputStream(dest.toString())
                ) {
                    write(in, out);
                }

            } else {
                Path dir = Paths.get(dest.toAbsolutePath().toString() + "/" + entry.getName());
                Files.createDirectories(dir);
            }
            System.out.println(" ------- OK");
        }
        zipFile.close();
    }

    static void extract(Path pathsourse, Path pathdest) throws IOException {
        src = pathsourse;
        dest = pathdest;
        extract();
    }
}

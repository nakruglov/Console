package com.kruglov;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

class FilesUtils {
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

    static class MyFileCopyVisitor extends SimpleFileVisitor<Path> {
        private Path source, destination;

        public MyFileCopyVisitor(Path s, Path d) {
            source = s;
            destination = d;
        }

        public FileVisitResult visitFile(Path path,
                                         BasicFileAttributes fileAttributes) {
            Path newd = destination.resolve(source.relativize(path));
            try {
                Files.copy(path, newd, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("copying file:" + path.getFileName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult preVisitDirectory(Path path,
                                                 BasicFileAttributes fileAttributes) {
            Path newd = destination.resolve(source.relativize(path));
            try {
                Files.copy(path, newd, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("copying directory: " + path.getFileName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return FileVisitResult.CONTINUE;
        }
    }

    static void copyDir(Path pathSource, Path pathDest) throws IOException {
        Path pathDestination = pathDest.resolve(pathSource.getFileName());
        if (Files.exists(pathDestination))
            System.out.println("Error: Directory " + pathDestination.toAbsolutePath().normalize() + " exists");
        else {
            try {
                Files.walkFileTree(pathSource, new MyFileCopyVisitor(pathSource, pathDestination));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("All Files Copied Successfully To " + pathDestination);
        }

    }

    static void del(Path path) throws IOException {
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                try (DirectoryStream<Path> dirStr = Files.newDirectoryStream(path)) {
                    for (Path child : dirStr) {
                        if (Files.isDirectory(child)) del(child);
                        else
                            Files.delete(child);
                    }
                    if (isEmptyDirectory(path)) Files.delete(path);
                }
            } else Files.delete(path);
        } else System.out.println("Directory or file does not exist");

    }


    public static void printDir(Path path) throws IOException {
        Set<Path> setfiles = new TreeSet();
        Set<Path> setdirs = new TreeSet();
        System.out.printf("%-70s%-10s%-30s%n", "Name", "Type", "Last Modify");
        try (DirectoryStream<Path> dirStr = Files.newDirectoryStream(path)) {
            for (Path child : dirStr) {
                if (Files.isDirectory(child)) setdirs.add(child);
                else
                    setfiles.add(child);
            }
            for (Path child : setdirs) {
                System.out.printf("%-70s%-10s%-30s%n", child.getFileName(), "Folder", Files.getLastModifiedTime(path));
            }
            for (Path child : setfiles) {
                System.out.printf("%-70s%-10s%-30s%n", child.getFileName(), "File", Files.getLastModifiedTime(path));
            }
            System.out.println("_______________________________________________________");
            if (path.getFileName() != null)
                System.out.println(path.getFileName().toString() + setdirs.size() + " directories " + setfiles.size() + " files");
            else
                System.out.println("Root " + path.toString() + " contains " + setdirs.size() + " directories " + setfiles.size() + " files");
        }
    }

    static void createDir(Path path) {
        try {
            Files.createDirectory(path);
            System.out.println("Directory  " + path.toAbsolutePath().normalize() + "  was created");
        } catch (IOException e) {
            System.out.println("Directory already exists");
        }
    }
}
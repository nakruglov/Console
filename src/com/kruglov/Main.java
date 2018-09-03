package com.kruglov;

import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    static boolean isValidSyntaxPath(String path) {
        Pattern p = Pattern.compile("([a-zA-Z]:[^:*<>|\"?]*)");
        Pattern p2 = Pattern.compile("([.[^:*<>|\"?]]+)");
        Matcher m = p.matcher(path);
        Matcher m2 = p2.matcher(path);
        return (m.matches() || m2.matches());
    }
    public static Path parse(String input, Path path) throws IOException {
        input = input.trim();
        int indexOfSpace = input.indexOf(" ");
        String[] args = null;
        if (indexOfSpace > 1) {
            args = input.substring(indexOfSpace).trim().split(":");
        } else args = new String[]{""};
        String command;
        String arg1 = "";
        String source = "";
        String dest = "";
        if (args.length == 2 && args[0].length() > 1) {
            if (args[0].substring(args[0].length() - 2, args[0].length() - 1).equals(" ")) {
                source = args[0].substring(0, args[0].length() - 2).trim();
                dest = args[0].substring(args[0].length() - 1) + ":" + args[1];
            }
        }
        if (args.length == 3 && args[1].length() > 1) {
            if (args[0].length() == 1) {
                source = args[0] + ":" + args[1].substring(0, args[1].length() - 2).trim();
            }
            dest = args[1].substring(args[1].length() - 1) + ":" + args[2];
        }
        if (args.length == 1) {
            source = args[0];
        }
        if (indexOfSpace > 1) command = input.substring(0, indexOfSpace).trim();
        else
            command = input.trim();
        if (indexOfSpace > 1) arg1 = input.substring(indexOfSpace, input.length()).trim();
        if (command.equals("dir")) {
            FilesUtils.printDir(path.toAbsolutePath().normalize());
            return path;
        } else if (command.equals("cd")) {
            if (isValidSyntaxPath((arg1)) && Files.exists(path.resolve(Paths.get(arg1)))) {
                path = path.resolve(Paths.get(arg1));
                return path;
            } else {
                System.out.println("Error: Directory does not exist");
            }
        } else if (command.equals("crdir")) {
            if (!arg1.equals("")) {
                if (isValidSyntaxPath(arg1) && !Files.exists(path.resolve(Paths.get(arg1)))) {
                    Path newDir = path.resolve(Paths.get(arg1));
                    FilesUtils.createDir(newDir);
                    return path;
                } else {
                    System.out.println("Error: Directory already exist");
                }
            } else System.out.println("Error: No path found");
        } else if (command.equals("copy") && args.length > 0) {
            if (isValidSyntaxPath(source)) {
                if (Files.exists(path.resolve(Paths.get(source)))) {
                    if (isValidSyntaxPath(dest)) {
                        if (Files.exists(path.resolve(Paths.get(dest)))) {
                            {
                                Path sourceCopy = path.resolve(Paths.get(source));
                                Path destCopy = Paths.get(dest);
                                FilesUtils.copyDir(sourceCopy, destCopy);
                            }
                        } else System.out.println("Error: Destination directory does not exist");
                    } else System.out.println("Error: Destination directory does not exist");
                } else System.out.println("Error: Source path has invalid symbols or empty");
            } else System.out.println("Error: Source path has invalid symbols or empty");
        } else if (command.equals("zip") && args.length > 0) {
            if (isValidSyntaxPath(source)) {
                if (Files.exists(path.resolve(Paths.get(source)))) {
                    if (isValidSyntaxPath(dest)) {
                        if (Files.exists(path.resolve(Paths.get(dest)))) {
                            {
                                Path sourceCopy = path.resolve(Paths.get(source));
                                Path destCopy = Paths.get(dest);
                                Path zip_dest = destCopy.resolve(sourceCopy.getFileName());
                                Zipper.Archive(sourceCopy, zip_dest);
                            }
                        } else System.out.println("Error: Destination directory does not exist");
                    } else System.out.println("Error: Destination path has invalid symbols or empty");
                } else System.out.println("Error: Source directory does not exist");
            } else System.out.println("Error: Source path has invalid symbols or empty");
        } else if (command.equals("unzip") && args.length > 0) {
            if (isValidSyntaxPath(source)) {
                if (Files.exists(path.resolve(Paths.get(source)))) {
                    if (isValidSyntaxPath(dest)) {
                        if (Files.exists(path.resolve(Paths.get(dest)))) {
                            {
                                Path sourceCopy = path.resolve(Paths.get(source));
                                Path destCopy = Paths.get(dest);
                                Zipper.extract(sourceCopy, destCopy);
                            }
                        } else System.out.println("Error: Destination directory does not exist");
                    } else System.out.println("Error: Destination directory has invalid symbols or empty");
                } else System.out.println("Error: ZIP file does not exist ");
            } else System.out.println("Error: Source path to ZIP file has invalid symbols or empty");
        } else if (command.equals("del")) {
            if (isValidSyntaxPath(arg1) && Files.exists(path.resolve(Paths.get(arg1)))) {

                Path delDir = path.resolve(Paths.get(arg1));
                FilesUtils.del(delDir);
                System.out.println("Directory " + delDir.toAbsolutePath().normalize() + " has been deleted");
                return path;
            } else {
                System.out.println("Error: Directory not found");
            }
        } else System.out.println("UNKNOWN COMMAND");
        return path;
    }
    public static void main(String[] args) throws IOException {
        Path path = Paths.get(".\\");
        System.out.print(path.toAbsolutePath().normalize() + ">");
        Scanner scanner = new Scanner(System.in);
        String input;
        while (scanner.hasNext()) {
            input = scanner.nextLine();
            if (input.equals("exit")) break;
            path = parse(input, path);
            System.out.print(path.toAbsolutePath().normalize() + ">");
        }
        scanner.close();
    }
}
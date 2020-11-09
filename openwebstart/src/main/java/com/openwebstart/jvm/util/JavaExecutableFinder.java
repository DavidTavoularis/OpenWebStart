package com.openwebstart.jvm.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Algorithm to find the java executable within a java home.
 */
public class JavaExecutableFinder {

    public static String findJavaExecutable(Path javaHome) {
        try {
            final Path binFolder = javaHome.resolve("bin");
            final List<String> possibleJavaExecutables = Files.find(binFolder, 1, (path, basicFileAttributes) -> isJavaExecutable(path))
                    .filter(Files::isRegularFile)
                    .filter(Files::isExecutable)
                    .map(Path::toAbsolutePath)
                    .map(Path::normalize)
                    .map(Path::toString)
                    .collect(Collectors.toList());

            if (possibleJavaExecutables.size() == 1) {
                return possibleJavaExecutables.get(0);
            }

            throw new IllegalStateException("found " + possibleJavaExecutables.size() + " java executables in " + javaHome);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isJavaExecutable(Path path) {
        final String fileName = path.getFileName().toString();
        return fileName.equalsIgnoreCase("java") || fileName.equalsIgnoreCase("java.exe");
    }
}

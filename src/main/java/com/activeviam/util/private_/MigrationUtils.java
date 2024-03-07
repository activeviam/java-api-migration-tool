/*
 * (C) ActiveViam 2023
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.util.private_;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utility class.
 *
 * @author ActiveViam
 */
public class MigrationUtils {

  /** System line separator. */
  public static final String LINE_SEPARATOR = System.lineSeparator();

  /** The source directory of the java classes to create the mapping from. */
  public static final String MAIN_DIRECTORY = "/src/main/java/";

  /** The suffix of a java file. */
  public static final String JAVA_SUFFIX = ".java";

  /** Extracts the name of the module inside the given path. */
  public static String extractModuleName(final String path) {
    return path.substring(0, path.indexOf(MAIN_DIRECTORY));
  }

  /** Returns the name of the file or directory denoted by the given path. */
  public static String getFileOrDirectoryName(final String path) {
    return new File(path).getName();
  }

  /**
   * Formats the given path to java import style.
   *
   * <p>Example: {@code module/path/src/main/java/package/name/className.java} -> {@code
   * package.name.className}.
   */
  public static String formatToImportString(final String path) {
    final int indexOfMainDirectory = path.indexOf(MAIN_DIRECTORY);
    final int indexOfJavaSuffix = path.length() - JAVA_SUFFIX.length();
    return path.substring(indexOfMainDirectory + MAIN_DIRECTORY.length(), indexOfJavaSuffix)
        .replace('/', '.');
  }

  /** Retrieves all the java files inside the given directory and its sub-directories. */
  public static List<Path> getAllJavaFiles(final String directory) {
    try {
      return Files.walk(Paths.get(directory))
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(JAVA_SUFFIX))
          .toList();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Returns the whole content of the given file as a single String. */
  public static String getFileContent(final Path filePath) {
    try {
      return Files.readString(filePath);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Replaces the whole content of the given file with the given content. */
  public static void replaceFileContent(final File file, final String newContent) {
    generateFile(file, writer -> writer.write(newContent));
  }

  /**
   * Creates a file and writes in it with the given {@link IWriteInstructions instructions}, or
   * overwrites the file if it already exists.
   */
  public static void generateFile(final File file, final IWriteInstructions writeInstructions) {
    MigrationUtils.createDirectoriesIfNeeded(file);
    try (final FileWriter writer = new FileWriter(file, false)) {
      writeInstructions.accept(writer);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates the directory and parent directories denoted by the given file if they do not exist
   * yet.
   *
   * <p>The given parameter must represent a file and not a directory.
   */
  private static void createDirectoriesIfNeeded(final File file) {
    final File directory = file.getParentFile();
    if (!directory.exists()) {
      directory.mkdirs();
    }
  }

  /**
   * Basically a {@link FileWriter} {@link Consumer} that can throw an {@link IOException}.
   *
   * @author ActiveViam
   */
  @FunctionalInterface
  public interface IWriteInstructions {

    void accept(FileWriter t) throws IOException;
  }

  /** Executes the given runnable and returns its execution time. */
  public static Duration runAndGetTime(final Runnable runnable) {
    final long startTimeInNanos = System.nanoTime();
    runnable.run();
    final long totalTimeInNanos = System.nanoTime() - startTimeInNanos;
    return Duration.ofNanos(totalTimeInNanos);
  }

  /** Checks that the given version does not contain a forbidden character. */
  public static void checkVersion(final String version) {
    if (version.contains("/") || version.contains("\\")) {
      throw new IllegalArgumentException("Version " + version + " contains a forbidden character.");
    }
  }

  /** Executes the given command line in the given directory. */
  public static void executeCommandLine(
      final String directoryPath, final boolean print, final String... command) {
    final ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.directory(new File(directoryPath));

    try {
      final Process process = processBuilder.start();

      // Read the output
      String line;
      if (print) {
        final BufferedReader reader =
            new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      }

      int exitCode = process.waitFor();

      // Check for errors
      if (print) {
        if (exitCode != 0) {
          final BufferedReader errorReader =
              new BufferedReader(new InputStreamReader(process.getErrorStream()));
          while ((line = errorReader.readLine()) != null) {
            System.err.println(line);
          }
        }
      }
    } catch (final IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}

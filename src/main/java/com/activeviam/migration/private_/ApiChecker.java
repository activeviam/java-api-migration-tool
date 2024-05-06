/*
 * (C) ActiveViam 2024
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.migration.private_;

import com.activeviam.util.private_.MigrationUtils;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Spots usage of non-public API in a project.
 *
 * @author ActiveViam
 */
public abstract class ApiChecker extends AFilesProcessor {

  private static final Map<String, Class<? extends ApiChecker>> LIBRARY_TO_API_CHECKER =
      Map.of("activepivot", ActivePivotApiChecker.class);

  private final Map<String, List<String>> matchingPatternsPerFile = new HashMap<>();

  /** Spots usage of non-public API of the given library among the given files. */
  public static String checkApi(final String libraryName, final List<Path> files) {
    // Get the corresponding ApiChecker
    final Class<? extends ApiChecker> apiCheckerClass = LIBRARY_TO_API_CHECKER.get(libraryName);
    if (apiCheckerClass != null) {
      final ApiChecker checker;
      try {
        checker = apiCheckerClass.getConstructor(List.class).newInstance(files);
      } catch (final InstantiationException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException
          | NoSuchMethodException
          | SecurityException e) {
        throw new RuntimeException(e);
      }

      final Duration executionTime = MigrationUtils.runAndGetTime(checker::processFiles);
      checker.info.setExecutionTime(executionTime);

      return checker.info
          + MigrationUtils.LINE_SEPARATOR
          + "Non-public API use in each file:"
          + MigrationUtils.LINE_SEPARATOR
          + printMatchingPatternsForEachFile(checker.matchingPatternsPerFile);
    } else {
      return "There is no implementation of "
          + ApiChecker.class.getName()
          + " for "
          + libraryName
          + " library.";
    }
  }

  private static String printMatchingPatternsForEachFile(
      final Map<String, List<String>> matchingPatternsPerFile) {
    if (matchingPatternsPerFile.isEmpty()) {
      return "You only use public API, congratulations !!!";
    }
    final StringBuilder sb = new StringBuilder();
    matchingPatternsPerFile.forEach(
        (fileName, matchingPatterns) -> {
          sb.append(fileName).append(MigrationUtils.LINE_SEPARATOR);
          matchingPatterns.forEach(
              matchingPattern ->
                  sb.append("\t").append(matchingPattern).append(MigrationUtils.LINE_SEPARATOR));
        });
    sb.deleteCharAt(sb.length() - 1); // Remove last line separator
    return sb.toString();
  }

  protected ApiChecker(
      final String name, final List<Path> files, final Set<String> patternsToMatch) {
    super(name, files, patternsToMatch);
  }

  @Override
  protected void processFile(final Path filePath) {
    final Matcher matcher = createMatcher(filePath);
    final List<String> matchingPatterns = processMatcher(matcher);
    if (!matchingPatterns.isEmpty()) {
      this.matchingPatternsPerFile.put(filePath.getFileName().toString(), matchingPatterns);
    }
  }

  private List<String> processMatcher(final Matcher matcher) {
    final List<String> matchingPatterns = new ArrayList<>();
    int matchingCounter = 0;
    while (matcher.find()) {
      matchingPatterns.add(matcher.group(0));
      ++matchingCounter;
    }
    this.info.addToMatchingCounter(matchingCounter);
    return matchingPatterns;
  }
}

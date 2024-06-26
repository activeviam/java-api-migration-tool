/*
 * (C) ActiveViam 2023-2024
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.migration.private_;

import com.activeviam.util.private_.MigrationUtils;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link #migrateFiles(List, Map) Migrate files} between two versions.
 *
 * @author ActiveViam
 */
public class FileMigrater extends AFilesProcessor {

  private static final String NAME = "MIGRATION";

  private final Map<String, String> mapping;

  /** Migrates the given files according to the given mapping between old and new imports. */
  public static PatternMatcherInfo migrateFiles(
      final List<Path> files, final Map<String, String> mapping) {
    final FileMigrater migrater = new FileMigrater(files, mapping);

    final Duration executionTime = MigrationUtils.runAndGetTime(migrater::processFiles);
    migrater.info.setExecutionTime(executionTime);

    return migrater.info;
  }

  private FileMigrater(final List<Path> files, final Map<String, String> mapping) {
    super(NAME, files, mapping.keySet());
    this.mapping = mapping;
  }

  @Override
  protected Pattern createPattern(final Set<String> patternsToMatch) {
    final String patternString =
        "("
            + String.join("|", patternsToMatch)
            + ")("
            + ALL_EXCEPT_ALPHANUMERIC_AND_UNDERSCORE
            + ")";
    return Pattern.compile(patternString);
  }

  @Override
  protected void processFile(final Path filePath) {
    final Matcher matcher = createMatcher(filePath);
    final String newContent = processMatcher(matcher);
    MigrationUtils.replaceFileContent(filePath.toFile(), newContent);
  }

  private String processMatcher(final Matcher matcher) {
    final StringBuilder stringBuffer = new StringBuilder();
    int matchingCounter = 0;

    while (matcher.find()) {
      /*
       * There is a bug if there is a backslash at the end of a String to replace in the file. It
       * causes this backslash to appear at the end of the replacement String, which throws an
       * IllegalArgumentException in Matcher#appendReplacement(StringBuilder, String). So we need to
       * check for this backslash every time and remove it from the replacement String to add it at
       * the end of the stringBuffer after the replacement has been appended.
       */
      String replacement = computeReplacementString(matcher);
      final boolean endWithBackslash = replacement.endsWith("\\");
      if (endWithBackslash) {
        // Remove the ending backslash from the replacement String
        replacement = replacement.substring(0, replacement.length() - 1);
      }

      matcher.appendReplacement(stringBuffer, replacement);

      if (endWithBackslash) {
        // Re-add the ending backslash after replacement
        stringBuffer.append("\\");
      }

      ++matchingCounter;
    }
    matcher.appendTail(stringBuffer);

    this.info.addToMatchingCounter(matchingCounter);

    return stringBuffer.toString();
  }

  private String computeReplacementString(final Matcher matcher) {
    return this.mapping.get(matcher.group(1)).concat(matcher.group(2));
  }
}

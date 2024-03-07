/*
 * (C) ActiveViam 2024
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.migration.private_;

import com.activeviam.util.private_.MigrationUtils;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Process files with a patter matcher.
 *
 * @author ActiveViam
 */
public abstract class AFilesProcessor {

  protected static final String ALL_EXCEPT_ALPHANUMERIC_AND_UNDERSCORE = "[^A-Za-z0-9_]";

  protected static final String ALPHANUMERIC_OR_UNDERSCORE_OR_DOT = "[A-Za-z0-9\\_\\.]";

  private final List<Path> files;

  protected final Pattern pattern; // Pattern is thread-safe

  protected final PatternMatcherInfo info;

  protected AFilesProcessor(
      final String name, final List<Path> files, final Set<String> patternsToMatch) {
    this.files = files;
    this.pattern = createPattern(patternsToMatch);
    this.info = new PatternMatcherInfo(name, this.files.size());
  }

  protected abstract Pattern createPattern(Set<String> patternsToMatch);

  protected void processFiles() {
    this.files.parallelStream().forEach(this::processFile);
  }

  protected abstract void processFile(Path filePath);

  protected Matcher createMatcher(final Path filePath) {
    final String currentContent = MigrationUtils.getFileContent(filePath);
    return this.pattern.matcher(currentContent);
  }
}

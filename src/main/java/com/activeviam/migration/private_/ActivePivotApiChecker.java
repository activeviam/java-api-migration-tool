/*
 * (C) ActiveViam 2024
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.migration.private_;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * {@link ApiChecker} for activepivot library.
 *
 * @author ActiveViam
 */
public class ActivePivotApiChecker extends ApiChecker {

  private static final String NAME = "ACTIVEPIVOT API CHECK";

  private static final Set<String> PATTERNS_TO_MATCH =
      Set.of("com\\.qfs", "com\\.quartetfs", "com\\.activeviam", "io\\.atoti");

  public ActivePivotApiChecker(final List<Path> files) {
    super(NAME, files, PATTERNS_TO_MATCH);
  }

  @Override
  protected Pattern createPattern(final Set<String> patternsToMatch) {
    final String patternString =
        "("
            + String.join("|", patternsToMatch)
            + ")\\."
            + ALPHANUMERIC_OR_UNDERSCORE_OR_DOT
            + "*("
            + String.join("|", "internal", "private_")
            + ")"
            + ALPHANUMERIC_OR_UNDERSCORE_OR_DOT
            + "*"
            + AFilesProcessor.ALL_EXCEPT_ALPHANUMERIC_AND_UNDERSCORE;
    return Pattern.compile(patternString);
  }
}

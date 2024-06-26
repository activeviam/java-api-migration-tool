/*
 * (C) ActiveViam 2023
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.migration.private_;

import com.activeviam.util.private_.MigrationUtils;
import java.time.Duration;

/**
 * Metadata information about a pattern matching process on a list of files.
 *
 * @author ActiveViam
 */
public class PatternMatcherInfo {

  private final String name;

  private final int numFiles;

  private Duration executionTime;

  private int matchingCounter = 0;

  /** Constructor. */
  PatternMatcherInfo(final String name, final int numFiles) {
    this.name = name;
    this.numFiles = numFiles;
  }

  /** Sets the total time it took to migrate all files. */
  void setExecutionTime(final Duration executionTime) {
    this.executionTime = executionTime;
  }

  /** Increases the matching counter to the given value. */
  void addToMatchingCounter(final int counter) {
    this.matchingCounter += counter;
  }

  @Override
  public String toString() {
    return this.name
        + " INFO"
        + MigrationUtils.LINE_SEPARATOR
        + "Number of processed files: "
        + this.numFiles
        + MigrationUtils.LINE_SEPARATOR
        + "Total execution time: "
        + this.executionTime
        + MigrationUtils.LINE_SEPARATOR
        + "Total matching patterns: "
        + this.matchingCounter;
  }
}

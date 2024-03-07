/*
 * (C) ActiveViam 2023
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.migration.app;

import com.activeviam.mapping.CsvMapping;
import com.activeviam.migration.FileMigrater;
import com.activeviam.migration.MigrationInfo;
import com.activeviam.util.MigrationUtils;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Launcher class to migrate project imports from a current to a target version.
 *
 * @author ActiveViam
 */
public class MigrationApplication {

  private static final Logger LOGGER = Logger.getLogger(MigrationApplication.class.getName());

  /** Default current version of the migration. */
  public static final String DEFAULT_CURRENT_VERSION = "6.0.12";

  /** Default target version of the migration. */
  public static final String DEFAULT_TARGET_VERSION = "6.1.0-alpha1";

  /** Default name of the bumped library. */
  public static final String DEFAULT_LIBRARY = "activepivot";

  /**
   * Migrates class imports in your java project.
   *
   * @param args specifies the path of the project to migrate if length 1, specifies project and
   *     current and target versions of the library if length 3, specifies project, versions and
   *     library name if length 4
   */
  public static void main(final String[] args) {
    // Get arguments
    final String projectPath;
    final String currentVersion;
    final String targetVersion;
    final String libraryName;
    if (args.length == 1) {
      projectPath = args[0];
      currentVersion = DEFAULT_CURRENT_VERSION;
      targetVersion = DEFAULT_TARGET_VERSION;
      libraryName = DEFAULT_LIBRARY;
    } else if (args.length == 3) {
      projectPath = args[0];
      currentVersion = args[1];
      targetVersion = args[2];
      libraryName = DEFAULT_LIBRARY;
    } else if (args.length == 4) {
      projectPath = args[0];
      currentVersion = args[1];
      targetVersion = args[2];
      libraryName = args[3];
    } else {
      throw new IllegalArgumentException(
          "Wrong number of arguments: " + args.length + ", expected 1, 3 or 4.");
    }

    // Check versions
    MigrationUtils.checkVersion(currentVersion);
    MigrationUtils.checkVersion(targetVersion);

    // Get the mapping from the corresponding csv file
    final Map<String, String> mapping =
        CsvMapping.loadMappingFromFile(libraryName, currentVersion, targetVersion);

    // Get the java files in the project to migrate
    final List<Path> files = MigrationUtils.getAllJavaFiles(projectPath);
    LOGGER.log(
        Level.INFO,
        () ->
            new StringBuilder("Process migration of ")
                .append(files.size())
                .append(" files in ")
                .append(projectPath)
                .append("...")
                .toString());

    // Update class imports in the files
    final MigrationInfo migrationInfo = FileMigrater.migrateFiles(files, mapping);

    // Print info about the migration
    LOGGER.log(Level.INFO, migrationInfo::toString);

    System.exit(0);
  }
}

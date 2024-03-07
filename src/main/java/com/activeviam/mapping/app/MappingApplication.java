/*
 * (C) ActiveViam 2023
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.mapping.app;

import static com.activeviam.util.JsonUtils.loadMappingFromJson;

import com.activeviam.mapping.CsvMapping;
import com.activeviam.mapping.Mapping;
import com.activeviam.mapping.MappingGenerator;
import com.activeviam.mapping.MappingPrinter;
import com.activeviam.migration.app.MigrationApplication;
import com.activeviam.util.MigrationUtils;
import java.util.Map;

/**
 * Launcher class to create a csv file representing a {@link Mapping} from a local git repository
 * between a current and a target version.
 *
 * @author ActiveViam
 */
public class MappingApplication {

  private static final String HARDCODED_MAPPING = "hardcoded_mapping.json";

  /**
   * Generates the mapping csv file.
   *
   * @param args specifies the path of the local git repository of the library if length 1,
   *     specifies repository and current and target versions of the library if length 3
   */
  public static void main(final String[] args) {
    // Get arguments
    final String repositoryPath;
    final String currentVersion;
    final String targetVersion;
    if (args.length == 1) {
      repositoryPath = args[0];
      currentVersion = MigrationApplication.DEFAULT_CURRENT_VERSION;
      targetVersion = MigrationApplication.DEFAULT_TARGET_VERSION;
    } else if (args.length == 3) {
      repositoryPath = args[0];
      currentVersion = args[1];
      targetVersion = args[2];
    } else {
      throw new IllegalArgumentException(
          "Wrong number of arguments: " + args.length + ", expected 1 or 3.");
    }

    // Check versions
    MigrationUtils.checkVersion(currentVersion);
    MigrationUtils.checkVersion(targetVersion);

    // Get hardcoded mapping
    final Map<String, String> hardcodedMapping = loadHardcodedMapping();

    // Create the mapping
    final Mapping mapping =
        MappingGenerator.generateMapping(
            repositoryPath, currentVersion, targetVersion, hardcodedMapping);

    // Print the mapping
    MappingPrinter.printMapping(mapping);

    // Create the csv mapping file
    CsvMapping.createFileFromMapping(mapping);

    System.exit(0);
  }

  private static Map<String, String> loadHardcodedMapping() {
    return loadMappingFromJson(HARDCODED_MAPPING);
  }
}

/*
 * (C) ActiveViam 2024
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.mapping.private_;

import static com.activeviam.util.private_.JsonUtils.loadMappingFromJson;

import com.activeviam.util.private_.MigrationUtils;
import java.util.Collections;
import java.util.Map;

/**
 * Load hard-coded mapping for {@value HardCodedMapping#LIBRARY} library to migrate to version
 * {@value HardCodedMapping#TARGET_VERSION}.
 *
 * @author ActiveViam
 */
public class HardCodedMapping {

  private static final String FILE_NAME = "hardcoded_mapping.json";
  private static final String LIBRARY = "activepivot";
  private static final String TARGET_VERSION = "6.1";

  /**
   * Loads a hard-coded mapping if the given repository and target version require it, otherwise
   * returns an empty map.
   */
  public static Map<String, String> loadHardcodedMapping(
      final String repositoryPath, final String targetVersion) {
    return shouldLoadHardcodedMapping(repositoryPath, targetVersion)
        ? loadMappingFromJson(FILE_NAME)
        : Collections.emptyMap();
  }

  private static boolean shouldLoadHardcodedMapping(
      final String repositoryPath, final String targetVersion) {
    final String libraryName = MigrationUtils.getFileOrDirectoryName(repositoryPath);
    return libraryName.equals(LIBRARY) && targetVersion.startsWith(TARGET_VERSION);
  }
}

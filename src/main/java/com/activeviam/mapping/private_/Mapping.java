/*
 * (C) ActiveViam 2023
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.mapping.private_;

import java.util.Map;

/**
 * POJO class holding a map representing the java files that have been moved or renamed between two
 * versions, and its corresponding {@link MappingInfo info}.
 *
 * <p>The mapping entries match the java import format.<br>
 * Example: {@code com.qfs.desc.IStoreDescription} -> {@code
 * com.activeviam.database.datastore.api.description.IStoreDescription}.
 *
 * @author ActiveViam
 */
public class Mapping {

  private final Map<String, String> mapping;

  private final MappingInfo info;

  /** Constructor. */
  public Mapping(final Map<String, String> mapping, final MappingInfo info) {
    this.mapping = mapping;
    this.info = info;
  }

  /**
   * Returns the mapping as a map where for each class the key is the import in the current version
   * and the value the import in the target version.
   */
  public Map<String, String> getMapping() {
    return this.mapping;
  }

  /** Returns the {@link MappingInfo}. */
  public MappingInfo getInfo() {
    return this.info;
  }
}

/*
 * (C) ActiveViam 2023-2024
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.mapping.private_;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.activeviam.util.private_.MigrationUtils;
import java.io.File;
import java.util.Map;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

@DisabledIf(
    value = "checkRepository",
    disabledReason = "Disabled because there is no local git repository to test.")
class TestMappingGeneratorWithActivePivot {

  private static final String ACTIVEPIVOT_REPOSITORY =
      "C:/Users/elie/eclipse-workspace-6.1/activepivot";

  @Test
  void testMappingBetween_6_0_9_And_6_1_0_alpha1() {
    final Map<String, String> mapping =
        MappingGenerator.generateMapping(ACTIVEPIVOT_REPOSITORY, "6.0.9", "6.1.0-alpha1")
            .getMapping();

    assertEquals(
        "com.activeviam.source.parquet.internal.impl.ParquetParser",
        mapping.get("com.activeviam.parquet.impl.ParquetParser"));
    assertEquals(
        "com.activeviam.source.parquet.internal.impl.ParquetParserBuilder",
        mapping.get("com.activeviam.parquet.impl.ParquetParserBuilder"));
    assertEquals(
        "com.activeviam.source.parquet.internal.parsers.DoubleArrayFieldParser",
        mapping.get("com.activeviam.parquet.parsers.impl.DoubleArrayFieldParser"));
    assertEquals(
        "com.activeviam.source.parquet.internal.parsers.FloatArrayFieldParser",
        mapping.get("com.activeviam.parquet.parsers.impl.FloatArrayFieldParser"));
    assertEquals(
        "com.activeviam.source.parquet.internal.parsers.IntegerArrayFieldParser",
        mapping.get("com.activeviam.parquet.parsers.impl.IntegerArrayFieldParser"));
    assertEquals(
        "com.activeviam.source.parquet.internal.parsers.LongArrayFieldParser",
        mapping.get("com.activeviam.parquet.parsers.impl.LongArrayFieldParser"));
    assertEquals(
        "com.activeviam.source.parquet.internal.parsers.StringArrayFieldParser",
        mapping.get("com.activeviam.parquet.parsers.impl.StringArrayFieldParser"));
    assertEquals(
        "com.activeviam.database.datastore.api.description.IDatastoreSchemaDescription",
        mapping.get("com.qfs.desc.IDatastoreSchemaDescription"));
    assertEquals(
        "com.activeviam.database.datastore.api.description.IDatastoreSchemaDescriptionBuilder",
        mapping.get("com.qfs.desc.IDatastoreSchemaDescriptionBuilder"));
    assertEquals(
        "com.activeviam.database.datastore.api.description.IStoreDescription",
        mapping.get("com.qfs.desc.IStoreDescription"));
    assertEquals(
        "com.activeviam.database.datastore.api.description.IReferenceDescriptionBuilder",
        mapping.get("com.qfs.desc.IReferenceDescriptionBuilder"));
    assertEquals(
        "com.activeviam.database.datastore.api.description.IReferenceDescription",
        mapping.get("com.qfs.desc.IReferenceDescription"));
  }

  @Test
  void testFetchRemoteTag_6_0_12() {
    final String tagName = "6.0.12";
    final Repository repository = MappingGenerator.getRepository(ACTIVEPIVOT_REPOSITORY);
    final boolean isAlreadyAbsent = MappingGenerator.isLocallyAbsent(repository, tagName);

    // Temporary delete the tag if it is already present
    if (!isAlreadyAbsent) {
      deleteVersion(ACTIVEPIVOT_REPOSITORY, tagName);
    }
    assertTrue(MappingGenerator.isLocallyAbsent(repository, tagName));

    // Check that the tag is well fetched
    assertThatNoException()
        .isThrownBy(
            () -> MappingGenerator.fetchIfNeeded(repository, ACTIVEPIVOT_REPOSITORY, tagName));
    assertFalse(MappingGenerator.isLocallyAbsent(repository, tagName));

    // Delete the tag if it was absent before the test
    if (isAlreadyAbsent) {
      deleteVersion(ACTIVEPIVOT_REPOSITORY, tagName);
      assertTrue(MappingGenerator.isLocallyAbsent(repository, tagName));
    }
  }

  // Makes sure you are not working on 6.0 branch if you run this test
  @Test
  void testFetchRemoteBranch_6_0() {
    final String branchName = "6.0";
    final Repository repository = MappingGenerator.getRepository(ACTIVEPIVOT_REPOSITORY);
    final boolean isAlreadyAbsent = MappingGenerator.isLocallyAbsent(repository, branchName);

    // Temporary delete the branch if it is already present
    if (!isAlreadyAbsent) {
      deleteVersion(ACTIVEPIVOT_REPOSITORY, branchName);
    }
    assertTrue(MappingGenerator.isLocallyAbsent(repository, branchName));

    // Check that the branch is well fetched
    assertThatNoException()
        .isThrownBy(
            () -> MappingGenerator.fetchIfNeeded(repository, ACTIVEPIVOT_REPOSITORY, branchName));
    assertFalse(MappingGenerator.isLocallyAbsent(repository, branchName));

    // Delete the branch if it was absent before the test
    if (isAlreadyAbsent) {
      deleteVersion(ACTIVEPIVOT_REPOSITORY, branchName);
      assertTrue(MappingGenerator.isLocallyAbsent(repository, branchName));
    }
  }

  private static void deleteVersion(final String repositoryPath, final String version) {
    // Try to delete as a tag first
    final String[] commandToDeleteTag = {"git", "tag", "-d", version};
    MigrationUtils.executeCommandLine(repositoryPath, false, commandToDeleteTag);

    // Then try as a branch
    final String[] commandToDeleteBranch = {"git", "branch", "-D", version};
    MigrationUtils.executeCommandLine(repositoryPath, false, commandToDeleteBranch);
  }

  static boolean checkRepository() {
    return ACTIVEPIVOT_REPOSITORY == null || !new File(ACTIVEPIVOT_REPOSITORY).isDirectory();
  }
}

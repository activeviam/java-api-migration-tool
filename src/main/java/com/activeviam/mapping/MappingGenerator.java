/*
 * (C) ActiveViam 2023-2024
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.mapping;

import com.activeviam.util.MigrationUtils;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

/**
 * Generate a {@link Mapping} of the files that have been moved or renamed between a current and a
 * target version in a local git repository.
 *
 * <p>May not include all renamed files.
 *
 * <p>Only include java files in {@link MigrationUtils#MAIN_DIRECTORY}.
 *
 * @author ActiveViam
 */
public class MappingGenerator {

  private static final String DUPLICATE_VALUE = "";

  private final Repository repository;

  private final RevCommit currentCommit;

  private final RevCommit targetCommit;

  private final MappingInfo mappingInfo;

  private Mapping mapping = null;

  /** Generates a {@link Mapping} from the given repository between the two given versions. */
  public static Mapping generateMapping(
      final String repositoryPath, final String currentVersion, final String targetVersion) {
    final MappingGenerator generator =
        new MappingGenerator(repositoryPath, currentVersion, targetVersion);

    final Map<String, String> hardcodedMapping =
        HardCodedMapping.loadHardcodedMapping(repositoryPath, targetVersion);

    final Duration executionTime =
        MigrationUtils.runAndGetTime(() -> generator.computeMapping(hardcodedMapping));
    generator.mappingInfo.setExecutionTime(executionTime);

    return generator.mapping;
  }

  private MappingGenerator(
      final String repositoryPath, final String currentVersion, final String targetVersion) {
    // Get repository
    this.repository = getRepository(repositoryPath);

    // Fetch versions if needed
    fetchIfNeeded(this.repository, repositoryPath, currentVersion);
    fetchIfNeeded(this.repository, repositoryPath, targetVersion);

    // Get commit ids
    this.currentCommit = getRevCommit(this.repository, currentVersion);
    this.targetCommit = getRevCommit(this.repository, targetVersion);

    // Initialize mapping info
    this.mappingInfo =
        new MappingInfo(
            MigrationUtils.getFileOrDirectoryName(repositoryPath), currentVersion, targetVersion);
  }

  static Repository getRepository(final String repositoryPath) {
    try {
      return new FileRepositoryBuilder()
          .findGitDir(new File(repositoryPath))
          .readEnvironment()
          .build();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Fetches the given version in the remote repository if it cannot be found locally.
   *
   * <p>Tries to fetch it as a tag first and then as a branch.
   */
  static void fetchIfNeeded(
      final Repository repository, final String repositoryPath, final String version) {
    if (isLocallyAbsent(repository, version)) {
      fetchTag(repositoryPath, version);
    }
    if (isLocallyAbsent(repository, version)) {
      fetchBranch(repositoryPath, version);
    }
    if (isLocallyAbsent(repository, version)) {
      throw new RuntimeException(
          "Version "
              + version
              + " cannot be found locally and remotely. Try to use a tag or branch name.");
    }
  }

  /** Checks whether the given version is absent on the given local repository. */
  static boolean isLocallyAbsent(final Repository repository, final String version) {
    try {
      return repository.findRef(version) == null;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Fetches the given tag (and only this one) in the given repository. */
  private static void fetchTag(final String repositoryPath, final String tagName) {
    final String[] commandToFetchTag = {
      "git", "fetch", "origin", "--no-tags", "refs/tags/" + tagName + ":refs/tags/" + tagName
    };
    MigrationUtils.executeCommandLine(repositoryPath, false, commandToFetchTag);
  }

  /** Fetches the given branch (and only this one) in the given repository. */
  private static void fetchBranch(final String repositoryPath, final String branchName) {
    final String[] commandToFetchBranch = {
      "git", "fetch", "origin", "+" + branchName + ":" + branchName
    };
    MigrationUtils.executeCommandLine(repositoryPath, false, commandToFetchBranch);
  }

  /**
   * Gets the {@link RevCommit commit reference} of the given version (can be a tag name, a branch
   * name, or a SHA-1) in the given repository.
   */
  private static RevCommit getRevCommit(final Repository repository, final String version) {
    final ObjectId commitId = getCommitId(repository, version);
    try (final RevWalk revWalk = new RevWalk(repository)) {
      try {
        return revWalk.parseCommit(commitId);
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static ObjectId getCommitId(final Repository repository, final String version) {
    try {
      return repository.resolve(version);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void computeMapping(final Map<String, String> hardcodedMapping) {
    final List<DiffEntry> diffEntries = getDiffEntries();
    final Map<String, String> mapping = computeMap(diffEntries);
    final Map<String, String> postProcessedMapping = postProcessMapping(mapping);
    postProcessedMapping.putAll(hardcodedMapping);
    this.mapping = new Mapping(postProcessedMapping, this.mappingInfo);
  }

  /** Returns all the {@link DiffEntry} between the two given commits. */
  private List<DiffEntry> getDiffEntries() {
    try (final ObjectReader reader = this.repository.newObjectReader()) {
      final AbstractTreeIterator currentTreeIterator =
          getTreeIterator(reader, this.currentCommit.getTree().getId());
      final AbstractTreeIterator targetTreeIterator =
          getTreeIterator(reader, this.targetCommit.getTree().getId());
      try (final DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
        diffFormatter.setRepository(this.repository);
        diffFormatter.setDetectRenames(true);
        return diffFormatter.scan(currentTreeIterator, targetTreeIterator);
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static AbstractTreeIterator getTreeIterator(
      final ObjectReader reader, final ObjectId id) {
    final CanonicalTreeParser treeIterator = new CanonicalTreeParser();
    try {
      treeIterator.reset(reader, id);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    return treeIterator;
  }

  private static Map<String, String> computeMap(final List<DiffEntry> diffEntries) {
    final Map<String, String> result = new HashMap<>();
    final Map<String, String> addedFiles = new HashMap<>();
    final Map<String, String> deletedFiles = new HashMap<>();

    for (final DiffEntry diffEntry : diffEntries) {
      if (skipDiffEntry(diffEntry)) {
        continue;
      }

      switch (diffEntry.getChangeType()) {
        case ADD:
          putIfAbsentOrReplaceWithDuplicateValue(diffEntry.getNewPath(), addedFiles);
          break;
        case DELETE:
          putIfAbsentOrReplaceWithDuplicateValue(diffEntry.getOldPath(), deletedFiles);
          break;
        case RENAME:
        case COPY:
          result.put(diffEntry.getOldPath(), diffEntry.getNewPath());
          break;
        default:
          throw new IllegalStateException("Unrecognized change type: " + diffEntry.getChangeType());
      }
    }

    // Put files that have been deleted then added in the result map
    // Iterate on the smallest map
    if (addedFiles.size() < deletedFiles.size()) {
      addedFiles.forEach(
          (fileName, newPath) -> {
            final String oldPath;
            if (deletedFiles.containsKey(fileName)
                && !newPath.equals(DUPLICATE_VALUE)
                && !(oldPath = deletedFiles.get(fileName)).equals(DUPLICATE_VALUE)) {
              result.put(oldPath, newPath);
            }
          });
    } else {
      deletedFiles.forEach(
          (fileName, oldPath) -> {
            final String newPath;
            if (addedFiles.containsKey(fileName)
                && !oldPath.equals(DUPLICATE_VALUE)
                && !(newPath = addedFiles.get(fileName)).equals(DUPLICATE_VALUE)) {
              result.put(oldPath, newPath);
            }
          });
    }

    return result;
  }

  /**
   * Returns whether a {@link DiffEntry} can be skipped in order to compute the map.
   *
   * <p>An entry can be skipped if:
   *
   * <ul>
   *   <li>It represents a modified file.
   *   <li>It is not a java file.
   *   <li>The path does not contain {@link MigrationUtils#MAIN_DIRECTORY}.
   * </ul>
   */
  private static boolean skipDiffEntry(final DiffEntry diffEntry) {
    if (diffEntry.getChangeType() == ChangeType.MODIFY) {
      return true;
    }

    final Side side;
    switch (diffEntry.getChangeType()) {
      case ADD:
      case RENAME:
      case COPY:
        side = Side.NEW;
        break;
      case DELETE:
        side = Side.OLD;
        break;
      default:
        throw new IllegalStateException("Unrecognized change type: " + diffEntry.getChangeType());
    }

    return !diffEntry.getMode(side).equals(FileMode.REGULAR_FILE)
        || !diffEntry.getPath(side).endsWith(MigrationUtils.JAVA_SUFFIX)
        || !diffEntry.getPath(side).contains(MigrationUtils.MAIN_DIRECTORY);
  }

  private static void putIfAbsentOrReplaceWithDuplicateValue(
      final String path, final Map<String, String> map) {
    final String fileName = MigrationUtils.getFileOrDirectoryName(path);
    if (map.containsKey(fileName)) {
      map.put(fileName, DUPLICATE_VALUE);
    } else {
      map.put(fileName, path);
    }
  }

  /**
   * {@link MigrationUtils#formatToImportString(String) Formats} the given mapping and computes the
   * provided {@link MappingInfo}.
   */
  private Map<String, String> postProcessMapping(final Map<String, String> mapping) {
    final Map<String, String> postProcessedMapping = new TreeMap<>();

    mapping.forEach(
        (oldPath, newPath) -> {
          final String oldImport = MigrationUtils.formatToImportString(oldPath);
          final String newImport = MigrationUtils.formatToImportString(newPath);
          if (!oldImport.equals(newImport)) {
            postProcessedMapping.put(oldImport, newImport);
            this.mappingInfo.process(oldPath, newPath);
          }
        });

    return postProcessedMapping;
  }
}

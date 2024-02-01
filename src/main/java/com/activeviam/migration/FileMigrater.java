/*
 * (C) ActiveViam 2023
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.migration;

import com.activeviam.util.MigrationUtils;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link #migrateFiles(List, Map) Migrate files} between two versions.
 *
 * @author ActiveViam
 */
public class FileMigrater {

	private static final String ALL_EXCEPT_ALPHANUMERIC_AND_UNDERSCORE = "([^A-Za-z0-9_])";

	private final List<Path> files;

	private final Map<String, String> mapping;

	private final MigrationInfo migrationInfo;

	private Pattern pattern = null;

	private FileMigrater(final List<Path> files, final Map<String, String> mapping) {
		this.files = files;
		this.mapping = mapping;
		this.migrationInfo = new MigrationInfo(this.files.size());
	}

	/**
	 * Migrates the given files according to the given mapping between old and new imports.
	 */
	public static MigrationInfo migrateFiles(final List<Path> files, final Map<String, String> mapping) {
		final FileMigrater migrater = new FileMigrater(files, mapping);

		final Duration executionTime = MigrationUtils.runAndGetTime(migrater::migrateFiles);
		migrater.migrationInfo.setExecutionTime(executionTime);

		return migrater.migrationInfo;
	}

	private void migrateFiles() {
		this.files.forEach(this::migrateFile);
	}

	private Pattern getOrCreatePattern() {
		if (this.pattern == null) {
			final String patternString = createPatternString();
			this.pattern = Pattern.compile(patternString);
		}
		return this.pattern;
	}

	private String createPatternString() {
		return new StringBuilder("(").append(String.join("|", this.mapping.keySet()))
				.append(")(")
				.append(ALL_EXCEPT_ALPHANUMERIC_AND_UNDERSCORE)
				.append(")")
				.toString();
	}

	private void migrateFile(final Path filePath) {
		final Matcher matcher = createMatcher(filePath);
		final String newContent = processMatcher(matcher);
		MigrationUtils.replaceFileContent(filePath.toFile(), newContent);
	}

	private Matcher createMatcher(final Path filePath) {
		final String currentContent = MigrationUtils.getFileContent(filePath);
		return getOrCreatePattern().matcher(currentContent);
	}

	private String processMatcher(final Matcher matcher) {
		final StringBuffer stringBuffer = new StringBuffer();
		int matchingCounter = 0;

		while (matcher.find()) {
			final String replacement = computeReplacementString(matcher);
			matcher.appendReplacement(stringBuffer, replacement);
			++matchingCounter;
		}
		matcher.appendTail(stringBuffer);

		this.migrationInfo.addToMatchingCounter(matchingCounter);

		return stringBuffer.toString();
	}

	private String computeReplacementString(final Matcher matcher) {
		return this.mapping.get(matcher.group(1)).concat(matcher.group(2));
	}

}

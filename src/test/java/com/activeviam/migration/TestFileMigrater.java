/*
 * (C) ActiveViam 2023
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.migration;

import com.activeviam.util.MigrationUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestFileMigrater {

	private static final String TEST_DIRECTORY = "src/test/resources/migration";

	private static final String TEST_FILE_NAME = "testFile";

	private static final Path TEST_FILE_PATH = Path.of(TEST_DIRECTORY, TEST_FILE_NAME);

	private static final String IMPORT_LINE_TEMPLATE = "import %s;";

	private static final String STATIC_IMPORT_LINE_TEMPLATE = "import static %s.staticMethod;";

	private static final String CODE_LINE_TEMPLATE = "public abstract %s method(int parameter);";

	private static final Map<String, String> MAPPING =
			new TreeMap<>(Map.of("a.b.Class", "a.c.Class", "a.b.ClassImpl", "a.c.d.ClassImpl"));

	@AfterAll
	static void deleteTestFile() {
		assert TEST_FILE_PATH.toFile().delete();
	}

	@Test
	void testMigrateFileWithImports() {
		generateTestFile(IMPORT_LINE_TEMPLATE);
		FileMigrater.migrateFiles(List.of(TEST_FILE_PATH), MAPPING);
		checkMigratedFile(IMPORT_LINE_TEMPLATE);
	}

	@Test
	void testMigrateFileWithStaticImports() {
		generateTestFile(STATIC_IMPORT_LINE_TEMPLATE);
		FileMigrater.migrateFiles(List.of(TEST_FILE_PATH), MAPPING);
		checkMigratedFile(STATIC_IMPORT_LINE_TEMPLATE);
	}

	@Test
	void testMigrateFileWithCode() {
		generateTestFile(CODE_LINE_TEMPLATE);
		FileMigrater.migrateFiles(List.of(TEST_FILE_PATH), MAPPING);
		checkMigratedFile(CODE_LINE_TEMPLATE);
	}

	private static void generateTestFile(final String lineTemplate) {
		MigrationUtils.generateFile(TEST_FILE_PATH.toFile(), writer -> {
			for (final String oldImport : MAPPING.keySet()) {
				writer.append(String.format(lineTemplate, oldImport)).append(MigrationUtils.LINE_SEPARATOR);
			}
		});
	}

	private static void checkMigratedFile(final String lineTemplate) {
		try {
			final List<String> lines = Files.readAllLines(TEST_FILE_PATH);

			int lineIndex = 0;
			for (final String newImport : MAPPING.values()) {
				Assertions.assertEquals(String.format(lineTemplate, newImport), lines.get(lineIndex++));
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

}

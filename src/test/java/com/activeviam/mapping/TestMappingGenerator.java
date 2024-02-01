/*
 * (C) ActiveViam 2023
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of ActiveViam. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */

package com.activeviam.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

class TestMappingGenerator {

	private static final String REPOSITORY = "C:/Users/elie/eclipse-workspace-6.1/activepivot";

	@Test
	@DisabledIf(
			value = "checkRepository",
			disabledReason = "Disabled because there is no local git repository to test.")
	void testBetween_6_0_9_And_6_1() {
		final Map<String, String> mapping =
				MappingGenerator.generateMapping(REPOSITORY, "6.0.9", "6.1").getMapping();

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

	static boolean checkRepository() {
		return REPOSITORY == null || !new File(REPOSITORY).isDirectory();
	}

}

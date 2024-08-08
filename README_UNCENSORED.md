# Java API Migration Tool

This project allows to automatically migrate class imports of your java project when you bump a library version.

It is composed of two parts:
 - mapping application
 - migration tool

Do not forget to run `mvn clean install` before running the tools.

## Mapping

The tool to generate the mapping from old packages to the new ones in the following format:
`old.package.name.ClassName` -> `new.package.name.ClassName`

Some renamed classes might also be included in the generated mapping.

Test classes are not included in the generated mapping.

You can use this tool with `generateMapping.sh` bash script (run `./generateMapping.sh <args>` in bash), or with the following command line:
`java -cp target/java-api-migration-tool-<version>-jar-with-dependencies.jar com.activeviam.mapping.api.MappingApplication <args>`
where `<version>` is the version of the migration tool you are using.

There are up to 3 parameters:
 - if 1 argument is provided, you can set the path to the local git repository of the library to create the mapping from
 - if 2 arguments are provided, you can set the path to the local git repository and the current version of the mapping
 - if 3 arguments are provided, you can set the path to the local git repository and the current and target versions of the mapping

Default values for the current and target versions are `6.0.15` and `6.1.0-rc2`.

Preferably use a tag or branch name when you provide a version.

Usage example: `./generateMapping.sh "/path/to/the/local/git/repository" "6.0.0" "6.1.0"`.

Mapping files are generated in `src/main/resources/mappings/<libraryName>` folder, and named like `6_0_0_to_6_1_0.csv`.

Note that some mapping files have already been generated. You can find them all in `src/main/resources`.

## Migration

The tool to migrate class imports with the generated mapping file.

You can use this tool with `migrate.sh` bash script (run `./migrate.sh <args>` in  bash), or with the following command line:
`java -cp target/java-api-migration-tool-<version>-jar-with-dependencies.jar com.activeviam.migration.api.MigrationApplication <args>`
where `<version>` is the version of the migration tool you are using.

There are up to 4 parameters:
 - if 1 argument is provided, you can set the path to the project to migrate
 - if 2 arguments are provided, you can set the path to the project and its current library version
 - if 3 arguments are provided, you can set the path to the project and the current and target versions of the mapping
 - if 4 arguments are provided, you can set the path to the project, the current and target versions, and the name of the library

Default values for the current and target versions are `6.0.15` and `6.1.0-rc2`, and `activepivot` for the library name.

Make sure there is a mapping file generated with these versions before trying to migrate.

Usage example: `./migrate.sh "/path/to/the/project/to/migrate" "6.0.0" "6.1.0" "libraryName"`.

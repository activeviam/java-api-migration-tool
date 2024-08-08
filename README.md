# How to use Java API Migration Tool to migrate Atoti

This project allows to automatically migrate class imports of your java project when you bump your atoti-server version.

It is composed of two parts:
 - mapping application
 - migration tool

Do not forget to run `mvn clean install` before running the tools.

## Basic usage

### Migrate your application from 6.0 to 6.1.0-rc2

Run `./migrate.sh "/path/to/the/project/to/migrate"`.

### Migrate your application from 5.11 to 6.1.0-rc2

Run `./migrate.sh "/path/to/the/project/to/migrate" "5.11"`.

### Migrate your application from 5.10 to 6.1.0-rc2

Run `./migrate.sh "/path/to/the/project/to/migrate" "5.10"`.

## Mapping

It is possible to generate mapping files with this tool (see `README_UNCENSORED.md`), but most relevant mapping files have already been generated.
You can find them all in `src/main/resources`.

## Migration

The tool to migrate class imports with the generated mapping file.

You can use this tool by running `./migrate.sh <args>` in  bash.

About the arguments:
 - if 1 argument is provided, you can set the path to the project to migrate
 - if 2 arguments are provided, you can set the path to the project and its current atoti-server version
 - if 3 arguments are provided, you can set the path to the project and the current and target atoti-server versions

Default values for the current and target versions are `6.0.15` and `6.1.0-rc2` when they are not provided.

Make sure there is a mapping file generated with these versions before trying to migrate.

Usage example: `./migrate.sh "/path/to/the/project/to/migrate" "6.0.0" "6.1.0"`.

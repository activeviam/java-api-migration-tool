#!/bin/bash

# Launch MigrationApplication main method
java -cp target/java-api-migration-tool-1.0-jar-with-dependencies.jar com.activeviam.migration.api.MigrationApplication "$@"

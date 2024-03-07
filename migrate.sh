#!/bin/bash

# Launch MigrationApplication main method
java -cp target/java-api-migration-tool-1.1-SNAPSHOT-jar-with-dependencies.jar com.activeviam.migration.api.MigrationApplication "$@"

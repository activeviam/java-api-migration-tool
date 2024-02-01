#!/bin/bash

# Launch MappingApplication main method (the end of the command line is meant to hide SLF4J messages)
java -cp target/java-api-migration-tool-1.0-SNAPSHOT.jar com.activeviam.mapping.app.MappingApplication "$@" 2>&1 | egrep -v "(^SLF4J)"
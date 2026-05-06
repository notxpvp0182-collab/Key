#!/bin/sh
#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Gradle wrapper script for Unix/macOS.
# Run: ./gradlew build

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Attempt to find JAVA_HOME if not set.
if [ -z "$JAVA_HOME" ]; then
  if command -v java > /dev/null 2>&1; then
    JAVA_CMD=java
  else
    echo "ERROR: JAVA_HOME is not set and no 'java' command found on PATH." >&2
    exit 1
  fi
else
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

# Determine script location.
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

exec "$JAVA_CMD" \
  -classpath "$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"

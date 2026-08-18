#!/bin/bash

# Find subprojects with a gradle.lockfile and execute the appropriate command
find . -mindepth 1 -maxdepth 1 -type d -exec sh -c '
    if [ -f "$1/gradle.lockfile" ]; then
      subproject=${1#./}
      echo "Running dependencies task for $subproject"
      ./gradlew ":$subproject:dependencies" --write-locks
    fi
' sh {} \;

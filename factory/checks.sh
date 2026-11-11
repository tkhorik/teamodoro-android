#!/usr/bin/env bash
# The repo's checks. Exit 0 only when the branch is fit to review.
set -euo pipefail

if [ -z "${JAVA_HOME:-}" ]; then
  if [ -x /usr/libexec/java_home ]; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home 2>/dev/null)"
  fi
fi

if [ ! -f local.properties ]; then
  REPO_ROOT="$(cd "$(git rev-parse --git-common-dir)/.." && pwd)"
  if [ -f "$REPO_ROOT/local.properties" ]; then
    cp "$REPO_ROOT/local.properties" local.properties
  elif [ -n "${ANDROID_HOME:-}" ]; then
    echo "sdk.dir=$ANDROID_HOME" > local.properties
  elif [ -d "$HOME/Library/Android/sdk" ]; then
    echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
  fi
fi

./gradlew assembleDebug testDebugUnitTest lintDebug

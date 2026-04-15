#!/usr/bin/env bash
# Build every publishable module (:library and :library-protect) for all targets
# (android release, iosArm64, iosX64, iosSimulatorArm64).
#
# Usage:
#   scripts/build.sh             # build only
#   scripts/build.sh --clean     # clean then build
#   scripts/build.sh <gradle-args...>

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

# ---------- JAVA_HOME ----------
# On macOS CI runners JAVA_HOME is often unset; auto-pick a JDK (21 > 17 > 11).
if [ -z "${JAVA_HOME:-}" ] && command -v /usr/libexec/java_home >/dev/null 2>&1; then
    for v in 21 17 11; do
        candidate="$(/usr/libexec/java_home -v "$v" 2>/dev/null || true)"
        if [ -n "$candidate" ] && [ -x "$candidate/bin/javac" ]; then
            export JAVA_HOME="$candidate"
            break
        fi
    done
fi
if [ -z "${JAVA_HOME:-}" ]; then
    echo "error: JDK 11+ required. Install with: brew install openjdk@21" >&2
    exit 1
fi

echo "JAVA_HOME=$JAVA_HOME"
java -version

# ---------- ANDROID_HOME ----------
# CI runners often provide ANDROID_SDK_ROOT but Gradle expects ANDROID_HOME.
if [ -z "${ANDROID_HOME:-}" ] && [ -n "${ANDROID_SDK_ROOT:-}" ]; then
    export ANDROID_HOME="$ANDROID_SDK_ROOT"
fi
if [ -n "${ANDROID_HOME:-}" ]; then
    export PATH="$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH"
    echo "ANDROID_HOME=$ANDROID_HOME"
fi

# ---------- args ----------
GRADLE_ARGS=("--console=plain" "--stacktrace")
TASKS=()
for arg in "$@"; do
    case "$arg" in
        --clean) TASKS+=("clean") ;;
        *)       GRADLE_ARGS+=("$arg") ;;
    esac
done

# Android release AAR + all three iOS klibs for each module. `assemble` on a
# KMP library covers every publishable target without pulling in test tasks.
TASKS+=(":library:assemble" ":library-protect:assemble")

echo "> ./gradlew ${TASKS[*]} ${GRADLE_ARGS[*]}"
./gradlew "${TASKS[@]}" "${GRADLE_ARGS[@]}"

echo "build ok"

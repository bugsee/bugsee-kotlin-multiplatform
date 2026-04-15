#!/usr/bin/env bash
# Run tests for every publishable module (:library and :library-protect) on CI
# simulators for both platforms:
#   - Android: Robolectric (JVM-hosted Android runtime simulator)
#   - iOS:     Xcode simulator — iosSimulatorArm64 on Apple Silicon, iosX64 on Intel
#
# Usage:
#   scripts/test.sh                  # run android + ios
#   scripts/test.sh android          # android only
#   scripts/test.sh ios              # ios only
#   scripts/test.sh <gradle-args...> # forward extra args to gradle

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

# ---------- JAVA_HOME ----------
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

# ---------- iOS simulator task (arch-detected) ----------
# We run the slice matching the host so the test binary can launch on a booted
# simulator without Rosetta. CI Mac runners are usually arm64 nowadays.
ARCH="$(uname -m)"
case "$ARCH" in
    arm64)   IOS_TEST_NAME="iosSimulatorArm64Test" ;;
    x86_64)  IOS_TEST_NAME="iosX64Test" ;;
    *) echo "error: unsupported host arch '$ARCH' for iOS simulator tests" >&2; exit 1 ;;
esac

# Every publishable KMP module must be covered on both CI simulators.
MODULES=(":library" ":library-protect")
IOS_TASKS=()
ANDROID_TASKS=()
for m in "${MODULES[@]}"; do
    IOS_TASKS+=("$m:$IOS_TEST_NAME")
    ANDROID_TASKS+=("$m:testDebugUnitTest")
done

# ---------- arg parsing ----------
RUN_ANDROID=1
RUN_IOS=1
GRADLE_ARGS=("--console=plain" "--stacktrace")
for arg in "$@"; do
    case "$arg" in
        android) RUN_ANDROID=1; RUN_IOS=0 ;;
        ios)     RUN_ANDROID=0; RUN_IOS=1 ;;
        all)     RUN_ANDROID=1; RUN_IOS=1 ;;
        *)       GRADLE_ARGS+=("$arg") ;;
    esac
done

# Only macOS hosts can run Kotlin/Native iOS tests. On Linux CI, skip iOS.
if [ "$RUN_IOS" = "1" ] && [ "$(uname -s)" != "Darwin" ]; then
    echo "note: host is $(uname -s) — skipping iOS tests (macOS required)"
    RUN_IOS=0
fi

# ---------- run ----------
ANDROID_STATUS="skipped"
IOS_STATUS="skipped"

if [ "$RUN_ANDROID" = "1" ]; then
    echo ""
    echo "=== Android tests (Robolectric): ${ANDROID_TASKS[*]} ==="
    if ./gradlew "${ANDROID_TASKS[@]}" "${GRADLE_ARGS[@]}"; then
        ANDROID_STATUS="passed"
    else
        ANDROID_STATUS="failed"
    fi
fi

if [ "$RUN_IOS" = "1" ]; then
    echo ""
    echo "=== iOS tests: ${IOS_TASKS[*]} ==="
    if ./gradlew "${IOS_TASKS[@]}" "${GRADLE_ARGS[@]}"; then
        IOS_STATUS="passed"
    else
        IOS_STATUS="failed"
    fi
fi

echo ""
echo "=== Summary ==="
printf "  %-10s %s\n" "android:" "$ANDROID_STATUS"
printf "  %-10s %s (%s on %s)\n" "ios:" "$IOS_STATUS" "$IOS_TEST_NAME" "$ARCH"
echo "  modules:   ${MODULES[*]}"
echo ""
echo "Reports: */build/reports/tests/"

if [ "$ANDROID_STATUS" = "failed" ] || [ "$IOS_STATUS" = "failed" ]; then
    exit 1
fi

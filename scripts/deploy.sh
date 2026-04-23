#!/usr/bin/env bash
# Publish both KMP modules to Maven Central (Sonatype Central Portal):
#   com.bugsee:bugsee-kotlin-multiplatform:<version from version.txt>
#   com.bugsee:bugsee-kotlin-multiplatform-protect:<version from version.txt>
#
# Targets per module: androidRelease, iosArm64, iosX64, iosSimulatorArm64, kotlinMultiplatform.
#
# Required credentials in ~/.gradle/gradle.properties (env-var form in parens
# works only for the maven-portal keys — the classic signing.* names contain
# dots and cannot be exported as shell env vars):
#   mavenCentralUsername       (ORG_GRADLE_PROJECT_mavenCentralUsername)
#   mavenCentralPassword       (ORG_GRADLE_PROJECT_mavenCentralPassword)
#   signing.keyId              # last 8 chars of the GPG key id
#   signing.password           # GPG key passphrase
#   signing.secretKeyRingFile  # absolute path to secring.gpg
#
# Release vs SNAPSHOT is controlled by the RELEASE env var (same convention as the
# legacy bugsee-android SDK):
#   RELEASE=true  -> publish version.txt value as-is (e.g. 0.1.0)
#   unset / false -> publish with a -SNAPSHOT suffix (e.g. 0.1.0-SNAPSHOT)
#
# Usage:
#   scripts/deploy.sh                     # SNAPSHOT publish + tests
#   RELEASE=true scripts/deploy.sh        # release publish + tests
#   scripts/deploy.sh --skip-tests        # publish without running tests (dangerous)
#   scripts/deploy.sh --local             # publishToMavenLocal (smoke test, RELEASE respected)

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
    echo "error: JDK 11+ required" >&2
    exit 1
fi

# ---------- ANDROID_HOME ----------
if [ -z "${ANDROID_HOME:-}" ] && [ -n "${ANDROID_SDK_ROOT:-}" ]; then
    export ANDROID_HOME="$ANDROID_SDK_ROOT"
fi
if [ -n "${ANDROID_HOME:-}" ]; then
    export PATH="$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH"
fi

# ---------- args ----------
SKIP_TESTS=0
LOCAL_ONLY=0
GRADLE_ARGS=("--console=plain" "--stacktrace")
for arg in "$@"; do
    case "$arg" in
        --skip-tests) SKIP_TESTS=1 ;;
        --local)      LOCAL_ONLY=1 ;;
        *)            GRADLE_ARGS+=("$arg") ;;
    esac
done

# ---------- version ----------
if [ ! -f version.txt ]; then
    echo "error: version.txt missing from repo root" >&2
    exit 1
fi
VERSION="$(head -n1 version.txt | tr -d '[:space:]')"
if [ -z "$VERSION" ]; then
    echo "error: version.txt is empty" >&2
    exit 1
fi

# RELEASE env var gates the -SNAPSHOT suffix (see header comment). Build files
# resolve this identically; the echo below is purely for the operator.
RELEASE_FLAG="${RELEASE:-false}"
case "$RELEASE_FLAG" in
    true|TRUE|1) RELEASE_FLAG=true ;;
    *)           RELEASE_FLAG=false ;;
esac
if [ "$RELEASE_FLAG" = "true" ]; then
    EFFECTIVE_VERSION="$VERSION"
else
    EFFECTIVE_VERSION="${VERSION}-SNAPSHOT"
fi
export RELEASE="$RELEASE_FLAG"

MODULES=(":library" ":library-protect")
echo "RELEASE=$RELEASE_FLAG  ->  publishing $EFFECTIVE_VERSION for: ${MODULES[*]}"

# ---------- credential sanity check (remote publish only) ----------
if [ "$LOCAL_ONLY" = "0" ]; then
    HOME_GRADLE_PROPS="${GRADLE_USER_HOME:-$HOME/.gradle}/gradle.properties"
    missing=()
    # Maven portal credentials: env vars OR gradle.properties.
    for key in mavenCentralUsername mavenCentralPassword; do
        env_name="ORG_GRADLE_PROJECT_$key"
        if [ -z "${!env_name:-}" ] && ! grep -qE "^[[:space:]]*$key=" "$HOME_GRADLE_PROPS" 2>/dev/null; then
            missing+=("$key")
        fi
    done
    # Classic signing.* keys: gradle.properties only (dots block env-var export).
    for key in signing.keyId signing.password signing.secretKeyRingFile; do
        if ! grep -qE "^[[:space:]]*${key//./\\.}=" "$HOME_GRADLE_PROPS" 2>/dev/null; then
            missing+=("$key")
        fi
    done
    if [ "${#missing[@]}" -gt 0 ]; then
        echo "error: missing credentials: ${missing[*]}" >&2
        echo "       add them to $HOME_GRADLE_PROPS (or export ORG_GRADLE_PROJECT_* for maven keys)" >&2
        exit 1
    fi
fi

# ---------- tests ----------
if [ "$SKIP_TESTS" = "0" ]; then
    echo ""
    echo "=== Running tests before publish ==="
    "$REPO_ROOT/scripts/test.sh"
else
    echo "note: --skip-tests set, skipping test run"
fi

# ---------- publish ----------
echo ""
PUBLISH_TASKS=()
if [ "$LOCAL_ONLY" = "1" ]; then
    for m in "${MODULES[@]}"; do PUBLISH_TASKS+=("$m:publishToMavenLocal"); done
    echo "=== publishToMavenLocal: ${PUBLISH_TASKS[*]} ==="
    ./gradlew "${PUBLISH_TASKS[@]}" "${GRADLE_ARGS[@]}"
    echo "published $EFFECTIVE_VERSION to $HOME/.m2/repository/com/bugsee/"
else
    for m in "${MODULES[@]}"; do PUBLISH_TASKS+=("$m:publishAndReleaseToMavenCentral"); done
    echo "=== publishAndReleaseToMavenCentral: ${PUBLISH_TASKS[*]} ==="
    ./gradlew "${PUBLISH_TASKS[@]}" "${GRADLE_ARGS[@]}"
    echo "released $EFFECTIVE_VERSION of ${MODULES[*]} to Maven Central"
fi

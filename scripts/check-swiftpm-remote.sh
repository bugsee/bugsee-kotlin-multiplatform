#!/usr/bin/env bash
# Fails when a committed SwiftPM manifest takes Bugsee from somewhere other than
# github.com/bugsee/spm. A build with bugsee.ios.localPackage set writes the local package
# path into the iosApp linkage package, and that change must not be committed.
set -euo pipefail
cd "$(dirname "$0")/.."

status=0
for manifest in \
    iosApp/KotlinMultiplatformLinkedPackage/subpackages/_library/Package.swift \
    .swiftpm-locks/default/swiftImport/subpackages/_library/Package.swift; do
    if ! grep -q 'url: "https://github.com/bugsee/spm.git"' "$manifest"; then
        echo "error: $manifest does not take Bugsee from https://github.com/bugsee/spm.git." >&2
        echo "       Was it built with bugsee.ios.localPackage? Restore it: git checkout -- $manifest" >&2
        status=1
    fi
done
exit "$status"

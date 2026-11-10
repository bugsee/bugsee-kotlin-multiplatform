#!/usr/bin/env bash
# Fails when a committed SwiftPM manifest takes Bugsee or its Feedback add-on from somewhere
# other than github.com/bugsee/spm / github.com/bugsee/feedback-spm. A build with
# bugsee.ios.localPackage or bugsee.ios.localFeedbackPackage set writes the local package path
# into the iosApp linkage package, and that change must not be committed.
set -euo pipefail
cd "$(dirname "$0")/.."

status=0
check() {
    local manifest="$1" url="$2"
    if ! grep -q "url: \"$url\"" "$manifest"; then
        echo "error: $manifest does not take its package from $url." >&2
        echo "       Was it built with bugsee.ios.localPackage or bugsee.ios.localFeedbackPackage?" >&2
        echo "       Restore it: git checkout -- $manifest" >&2
        status=1
    fi
}

check iosApp/KotlinMultiplatformLinkedPackage/subpackages/_library/Package.swift \
    https://github.com/bugsee/spm.git
check .swiftpm-locks/default/swiftImport/subpackages/_library/Package.swift \
    https://github.com/bugsee/spm.git
check iosApp/KotlinMultiplatformLinkedPackage/subpackages/_library_feedback/Package.swift \
    https://github.com/bugsee/feedback-spm.git
check .swiftpm-locks/default/swiftImport/subpackages/_library-feedback/Package.swift \
    https://github.com/bugsee/feedback-spm.git
check .swiftpm-locks/default/swiftImport/subpackages/_library_feedback/Package.swift \
    https://github.com/bugsee/feedback-spm.git
exit "$status"

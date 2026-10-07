package com.bugsee.kmp

/**
 * Depth of memory leak detection ([BugseeOptions.DetectMemoryLeaksMode]). Android only; requires
 * the `bugsee-android-leak` module.
 */
public enum class BugseeMemoryLeakMode {
    /**
     * Retained-object detection without heap dumps.
     */
    Fast,

    /**
     * Adds an in-process heap dump and leak-trace analysis, on debuggable builds only.
     */
    DeepDebugOnly,

    /**
     * Adds heap dump analysis in production builds too.
     */
    DeepEverywhere
}

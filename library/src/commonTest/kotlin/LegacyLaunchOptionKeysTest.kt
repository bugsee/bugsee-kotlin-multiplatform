package com.bugsee.kmp

import com.bugsee.kmp.internal.Platform
import com.bugsee.kmp.internal.PlatformInfo
import com.bugsee.kmp.internal.legacyLaunchOptionKeys
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Launch option keys from before 0.2.0 are ignored with an error naming the new key (#57). The
 * store works for the platform the tests run on: they run on Android and on iOS.
 */
class LegacyLaunchOptionKeysTest {

    private val isAndroid = PlatformInfo.getPlatformType() == Platform.ANDROID

    private class Logs {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
    }

    private fun store(logs: Logs) =
        BugseeLaunchOptionsStore(logError = { logs.errors += it }, logWarning = { logs.warnings += it })

    /** The single error that putting [key] logs. */
    private fun errorFor(key: String, value: Any): String {
        val logs = Logs()
        store(logs).put(key, value)
        return logs.errors.single()
    }

    @Test
    fun `a key from before 0_2_0 is not sent and the error names the new key`() {
        val logs = Logs()
        val store = store(logs)

        store.put("VideoEnabled", false)

        val map = store.toMap()
        assertFalse("VideoEnabled" in map)
        assertEquals(true, map[BugseeOptions.CaptureVideo]) // the default, and the only key for the option
        assertEquals(
            listOf(
                "Ignoring launch option 'VideoEnabled', a key from before 0.2.0: " +
                    "use BugseeOptions.CaptureVideo (\"com.bugsee.option.capture.video\") instead",
            ),
            logs.errors,
        )
        assertEquals(emptyList(), logs.warnings)
    }

    @Test
    fun `the error for a removed option says so`() {
        assertEquals(
            "Ignoring launch option 'BugseeKillDetectionKey', a key from before 0.2.0: SDK 7 removed its option; " +
                "see BugseeOptions.DetectAndReportExit (\"com.bugsee.option.detect.exit\") instead",
            errorFor("BugseeKillDetectionKey", true),
        )
    }

    @Test
    fun `the error says which SDK has an option the platform does not`() {
        if (isAndroid) {
            assertEquals(
                "Ignoring launch option 'MaxFrameRate', a key from before 0.2.0: its option is now " +
                    "BugseeOptions.CaptureVideoMaxFrameRate (\"com.bugsee.option.capture.video.max-frame-rate\"), " +
                    "which only the iOS SDK has",
                errorFor("MaxFrameRate", 30),
            )
            assertEquals(
                "Ignoring launch option 'BugseeEnableMachExceptions', a key from before 0.2.0: SDK 7 removed its " +
                    "option; see BugseeOptions.CaptureCrashHandlerType (\"com.bugsee.option.capture.crash-handler-type\"), " +
                    "which only the iOS SDK has",
                errorFor("BugseeEnableMachExceptions", true),
            )
        } else {
            assertEquals(
                "Ignoring launch option 'NdkCrashReport', a key from before 0.2.0: its option is now " +
                    "BugseeOptions.NdkDetectAndReport (\"com.bugsee.option.detect.crash-ndk\"), " +
                    "which only the Android SDK has",
                errorFor("NdkCrashReport", true),
            )
            assertEquals(
                "Ignoring launch option 'HandleAnr', a key from before 0.2.0: SDK 7 removed its option; " +
                    "see BugseeOptions.DetectAndReportExitNotResponding " +
                    "(\"com.bugsee.option.detect.exit.not_responding\"), which only the Android SDK has",
                errorFor("HandleAnr", true),
            )
        }
    }

    @Test
    fun `the error for a key without an SDK 7 option says so`() {
        assertEquals(
            "Ignoring launch option 'UseSdCard', a key from before 0.2.0: SDK 7 has no such option",
            errorFor("UseSdCard", true),
        )
    }

    @Test
    fun `no key from before 0_2_0 is sent or changes an option`() {
        val logs = Logs()
        val store = store(logs)
        val defaults = BugseeOptionsSpec.defaults(isAndroid)

        // A value the new option could take, other than its default, so a translated key would show.
        legacyLaunchOptionKeys.forEach { (key, legacy) ->
            val value = when (val default = legacy.replacement?.let { defaults[it.key] }) {
                is Boolean -> !default
                is Int -> default + 1
                is Float -> default + 1f
                else -> "legacy"
            }
            store.put(key, value)
        }

        assertEquals(BugseeLaunchOptionsStore().toMap(), store.toMap())
        assertEquals(legacyLaunchOptionKeys.size, logs.errors.size)
        assertEquals(emptyList(), logs.warnings)
    }

    @Test
    fun `the raw map constructor and setCustomOption ignore keys from before 0_2_0`() {
        val options = BugseeLaunchOptions(mapOf("VideoEnabled" to false, "MaxRecordingTime" to 120, "CrashReport" to false))
        options.setCustomOption("ScreenshotEnabled", false)

        assertEquals(BugseeLaunchOptions().toMap(), options.toMap())
    }

    @Test
    fun `every new key is an option and no key from before 0_2_0 is one`() {
        legacyLaunchOptionKeys.forEach { (key, legacy) ->
            legacy.replacement?.let { assertTrue(it.key in BugseeOptionsSpec.types, "$key: ${it.key}") }
            assertFalse(key in BugseeOptionsSpec.types, key)
            assertFalse(key.startsWith("com.bugsee.option."), key)
        }
    }

    @Test
    fun `another key outside com_bugsee_option is passed with a warning`() {
        val logs = Logs()
        val store = store(logs)

        store.put("myKey", 42)
        // iOS SDK 7 reads it as an older name of com.bugsee.option.$$ENDPOINT, which is sent only when set.
        store.put("endpoint", "https://example.com")

        val map = store.toMap()
        assertEquals(42, map["myKey"])
        assertEquals("https://example.com", map["endpoint"])
        assertEquals(
            listOf(
                "Launch option 'myKey' is not a Bugsee option key (com.bugsee.option.*); it is passed to the native SDK as is",
                "Launch option 'endpoint' is not a Bugsee option key (com.bugsee.option.*); it is passed to the native SDK as is",
            ),
            logs.warnings,
        )
        assertEquals(emptyList(), logs.errors)
    }

    @Test
    fun `com_bugsee_option keys are passed without a warning`() {
        val logs = Logs()
        val store = store(logs)

        store.put("com.bugsee.option.\$\$ENDPOINT", "https://example.com")
        store.put("com.bugsee.option.capture.not-an-option", false)
        store.put(BugseeOptions.Duration, 30)

        val map = store.toMap()
        assertEquals("https://example.com", map["com.bugsee.option.\$\$ENDPOINT"])
        assertEquals(false, map["com.bugsee.option.capture.not-an-option"])
        assertEquals(30, map[BugseeOptions.Duration])
        assertEquals(emptyList(), logs.warnings)
        assertEquals(emptyList(), logs.errors)
    }

    @Test
    fun `a custom value that is dropped gets no warning`() {
        val logs = Logs()
        val store = store(logs)

        store.put("myKey", listOf(1))

        assertFalse("myKey" in store.toMap())
        assertEquals(emptyList(), logs.warnings)
    }
}

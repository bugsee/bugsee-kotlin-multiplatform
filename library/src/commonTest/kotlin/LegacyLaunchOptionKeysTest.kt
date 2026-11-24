package com.bugsee.kmp

import com.bugsee.kmp.internal.legacyLaunchOptionKeys
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Launch option keys from before 0.2.0 are ignored with an error naming the new key (#57). */
class LegacyLaunchOptionKeysTest {

    private class Logs {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
    }

    private fun store(isAndroid: Boolean, logs: Logs) =
        BugseeLaunchOptionsStore(isAndroid, logError = { logs.errors += it }, logWarning = { logs.warnings += it })

    private val platforms = listOf(true, false)

    @Test
    fun `a key from before 0_2_0 is not sent and the error names the new key`() {
        for (isAndroid in platforms) {
            val logs = Logs()
            val store = store(isAndroid, logs)

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
    }

    @Test
    fun `the error for a removed option says so`() {
        val logs = Logs()

        store(isAndroid = true, logs).put("HandleAnr", true)

        assertEquals(
            listOf(
                "Ignoring launch option 'HandleAnr', a key from before 0.2.0: SDK 7 removed its option; " +
                    "see BugseeOptions.DetectAndReportExitNotResponding " +
                    "(\"com.bugsee.option.detect.exit.not_responding\") instead",
            ),
            logs.errors,
        )
    }

    @Test
    fun `no key from before 0_2_0 is sent or changes an option`() {
        for (isAndroid in platforms) {
            val logs = Logs()
            val store = store(isAndroid, logs)

            legacyLaunchOptionKeys.keys.forEach { store.put(it, false) }

            assertEquals(BugseeLaunchOptionsStore(isAndroid).toMap(), store.toMap())
            assertEquals(legacyLaunchOptionKeys.size, logs.errors.size)
            assertEquals(emptyList(), logs.warnings)
        }
    }

    @Test
    fun `the raw map constructor and setCustomOption ignore keys from before 0_2_0`() {
        val options = BugseeLaunchOptions(mapOf("VideoEnabled" to false, "MaxRecordingTime" to 120, "CrashReport" to false))
        options.setCustomOption("ScreenshotEnabled", false)

        assertEquals(BugseeLaunchOptions().toMap(), options.toMap())
        // The iOS wrapper installs its Kotlin crash hook from this property.
        assertTrue(options.detectAndReportCrash)
    }

    @Test
    fun `every new key is an option and no key from before 0_2_0 is one`() {
        legacyLaunchOptionKeys.forEach { (key, legacy) ->
            assertTrue(legacy.key in BugseeOptionsSpec.types, "$key: ${legacy.key}")
            assertFalse(key in BugseeOptionsSpec.types, key)
            assertFalse(key.startsWith("com.bugsee.option."), key)
        }
    }

    @Test
    fun `another key outside com_bugsee_option is passed with a warning`() {
        for (isAndroid in platforms) {
            val logs = Logs()
            val store = store(isAndroid, logs)

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
    }

    @Test
    fun `com_bugsee_option keys are passed without a warning`() {
        for (isAndroid in platforms) {
            val logs = Logs()
            val store = store(isAndroid, logs)

            store.put("com.bugsee.option.\$\$ENDPOINT", "https://example.com")
            store.put("com.bugsee.option.capture.mach-exceptions", false)
            store.put(BugseeOptions.Duration, 30)

            val map = store.toMap()
            assertEquals("https://example.com", map["com.bugsee.option.\$\$ENDPOINT"])
            assertEquals(false, map["com.bugsee.option.capture.mach-exceptions"])
            assertEquals(30, map[BugseeOptions.Duration])
            assertEquals(emptyList(), logs.warnings)
            assertEquals(emptyList(), logs.errors)
        }
    }

    @Test
    fun `a custom value that is dropped gets no warning`() {
        val logs = Logs()
        val store = store(isAndroid = true, logs)

        store.put("myKey", listOf(1))

        assertFalse("myKey" in store.toMap())
        assertEquals(emptyList(), logs.warnings)
    }
}

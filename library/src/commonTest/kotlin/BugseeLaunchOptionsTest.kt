package com.bugsee.kmp

import com.bugsee.kmp.internal.Platform
import com.bugsee.kmp.internal.PlatformInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BugseeLaunchOptionsTest {

    private val isAndroid = PlatformInfo.getPlatformType() == Platform.ANDROID

    // --- defaults ---

    @Test
    fun `defaults match the native SDK manifests`() {
        val options = BugseeLaunchOptions()

        assertEquals(60, options.duration)
        assertTrue(options.reportingTriggerByShake)
        assertFalse(options.reportingTriggerByScreenshot)
        assertEquals(BugseeFrameRate.High, options.captureVideoFrameRate)
        assertEquals(BugseeLogLevel.Verbose, options.captureLogsLevel)
        assertEquals(BugseeSeverity.Blocker, options.reportingDefaultCrashPriority)
        assertEquals(0.01f, options.performanceSampleRate)
    }

    @Test
    fun `defaults differ per platform where the SDKs differ`() {
        val options = BugseeLaunchOptions()

        assertEquals(if (isAndroid) 150 else 50, options.maxDataSize)
        assertEquals(!isAndroid, options.captureVideoAdaptive)
    }

    @Test
    fun `options of the other platform report that platform's default`() {
        val options = BugseeLaunchOptions()

        // Android only
        assertTrue(options.ndkDetectAndReport)
        assertEquals(BugseeMemoryLeakMode.Fast, options.detectMemoryLeaksMode)
        // iOS only
        assertFalse(options.captureAVPlayer)
        assertNull(options.buildTarget)
    }

    // --- toMap ---

    @Test
    fun `toMap sends every default the current platform knows`() {
        val map = BugseeLaunchOptions().toMap()

        BugseeOptionsSpec.defaults(isAndroid).keys.forEach { key ->
            assertTrue(key in map, "missing default for $key")
        }
        assertEquals(60, map[BugseeOptions.Duration])
        assertEquals(if (isAndroid) 150 else 50, map[BugseeOptions.MaxDataSize])
    }

    @Test
    fun `toMap never sends options of the other platform`() {
        val options = BugseeLaunchOptions()
        options.ndkDetectAndReport = false
        options.captureAVPlayer = true

        val map = options.toMap()

        if (isAndroid) {
            assertEquals(false, map[BugseeOptions.NdkDetectAndReport])
            assertFalse(BugseeOptions.CaptureAVPlayer in map)
        } else {
            assertEquals(true, map[BugseeOptions.CaptureAVPlayer])
            assertFalse(BugseeOptions.NdkDetectAndReport in map)
        }
        // The getter still returns what was set
        assertFalse(options.ndkDetectAndReport)
        assertTrue(options.captureAVPlayer)
    }

    @Test
    fun `toMap sends set values`() {
        val options = BugseeLaunchOptions()
        options.duration = 120
        options.captureNetwork = false
        options.captureVideoScale = 0.5f

        val map = options.toMap()

        assertEquals(120, map[BugseeOptions.Duration])
        assertEquals(false, map[BugseeOptions.CaptureNetwork])
        assertEquals(0.5f, map[BugseeOptions.CaptureVideoScale])
    }

    @Test
    fun `toMap sends enums as Android constant names or iOS Int values`() {
        val options = BugseeLaunchOptions()
        options.captureVideoFrameRate = BugseeFrameRate.Low
        options.reportingDefaultBugPriority = BugseeSeverity.VeryLow

        val map = options.toMap()

        if (isAndroid) {
            assertEquals("Low", map[BugseeOptions.CaptureVideoFrameRate])
            assertEquals("VeryLow", map[BugseeOptions.ReportingDefaultBugPriority])
        } else {
            assertEquals(1, map[BugseeOptions.CaptureVideoFrameRate])
            assertEquals(1, map[BugseeOptions.ReportingDefaultBugPriority])
        }
    }

    @Test
    fun `toMap includes wrapper info`() {
        val wrapperInfo = BugseeLaunchOptions().toMap()["wrapper_info"] as Map<*, *>

        assertEquals("kmp", wrapperInfo["type"])
        assertEquals(BuildKonfig.libraryVersion, wrapperInfo["version"])
        assertEquals(BuildKonfig.buildChecksum, wrapperInfo["build"])
        assertEquals(KotlinVersion.CURRENT.toString(), wrapperInfo["runtime"])
    }

    // --- nullable string options ---

    @Test
    fun `a nullable option is sent only once set`() {
        val options = BugseeLaunchOptions()
        assertFalse(BugseeOptions.BuildTarget in options.toMap())

        options.buildTarget = "Release"
        assertEquals("Release", options.buildTarget)
        assertEquals(!isAndroid, BugseeOptions.BuildTarget in options.toMap())

        options.buildTarget = null
        assertNull(options.buildTarget)
        assertFalse(BugseeOptions.BuildTarget in options.toMap())
    }

    // --- custom options and raw maps ---

    @Test
    fun `custom options pass through unchanged`() {
        val options = BugseeLaunchOptions()
        options.setCustomOption("com.bugsee.option.\$\$ENDPOINT", "https://example.com")
        options.setCustomOption("myKey", 42)

        val map = options.toMap()

        assertEquals("https://example.com", map["com.bugsee.option.\$\$ENDPOINT"])
        assertEquals(42, map["myKey"])
    }

    @Test
    fun `a custom option with a known key sets the property`() {
        val options = BugseeLaunchOptions()
        options.setCustomOption(BugseeOptions.Duration, 30)

        assertEquals(30, options.duration)
        assertEquals(30, options.toMap()[BugseeOptions.Duration])
    }

    @Test
    fun `wrapper info cannot be overridden`() {
        val options = BugseeLaunchOptions()
        options.setCustomOption("wrapper_info", "fake")

        assertTrue(options.toMap()["wrapper_info"] is Map<*, *>)
    }

    @Test
    fun `raw map sets properties and keeps unknown keys`() {
        val options = BugseeLaunchOptions(
            mapOf(
                BugseeOptions.Duration to 45.0, // numbers are coerced
                BugseeOptions.CaptureVideoFrameRate to "medium", // enums resolve by name
                BugseeOptions.CaptureLogs to false,
                "custom" to "value",
            )
        )

        assertEquals(45, options.duration)
        assertEquals(BugseeFrameRate.Medium, options.captureVideoFrameRate)
        assertFalse(options.captureLogs)
        assertEquals("value", options.toMap()["custom"])
    }

    @Test
    fun `invalid raw values fall back to the default`() {
        val options = BugseeLaunchOptions(
            mapOf(
                BugseeOptions.CaptureLogs to "yes",
                BugseeOptions.CaptureVideoFrameRate to "Ultra",
            )
        )

        assertTrue(options.captureLogs)
        assertEquals(BugseeFrameRate.High, options.captureVideoFrameRate)
    }

    @Test
    fun `invalid raw values are not sent to the native SDK`() {
        val map = BugseeLaunchOptions(
            mapOf(
                BugseeOptions.CaptureLogs to "yes",
                BugseeOptions.Duration to "long",
                BugseeOptions.CaptureVideoFrameRate to BugseeVideoQuality.High, // wrong enum
            )
        ).toMap()

        assertEquals(true, map[BugseeOptions.CaptureLogs])
        assertEquals(60, map[BugseeOptions.Duration])
        assertEquals(if (isAndroid) "High" else 3, map[BugseeOptions.CaptureVideoFrameRate])
    }

    @Test
    fun `an invalid value keeps the previous one`() {
        val options = BugseeLaunchOptions()
        options.duration = 30
        options.setCustomOption(BugseeOptions.Duration, "forever")

        assertEquals(30, options.duration)
        assertEquals(30, options.toMap()[BugseeOptions.Duration])
    }

    @Test
    fun `numbers are converted to the option type`() {
        val options = BugseeLaunchOptions(
            mapOf(
                BugseeOptions.CaptureVideoScale to 1, // Int for a Float option
                BugseeOptions.Duration to 45L,
            )
        )
        val map = options.toMap()

        assertEquals(1.0f, map[BugseeOptions.CaptureVideoScale])
        assertEquals(45, map[BugseeOptions.Duration])
    }

    @Test
    fun `every enum option is sent in the native representation`() {
        val map = BugseeLaunchOptions().toMap()
        val enumKeys = BugseeOptionsSpec.types
            .filterValues { it is BugseeOptionType.EnumType }
            .keys
            .filter { it in BugseeOptionsSpec.knownKeys(isAndroid) }

        assertTrue(enumKeys.isNotEmpty())
        enumKeys.forEach { key ->
            val value = map[key]
            if (isAndroid) {
                assertTrue(value is String, "$key should be sent as a constant name, was $value")
            } else {
                assertTrue(value is Int, "$key should be sent as an Int, was $value")
            }
        }
    }

    // --- spec ---

    @Test
    fun `spec covers both platforms`() {
        assertTrue(BugseeOptionsSpec.knownKeys(isAndroid = true).all { it in BugseeOptionsSpec.allKeys })
        assertTrue(BugseeOptionsSpec.knownKeys(isAndroid = false).all { it in BugseeOptionsSpec.allKeys })
        assertEquals(
            BugseeOptionsSpec.allKeys,
            BugseeOptionsSpec.knownKeys(isAndroid = true) + BugseeOptionsSpec.knownKeys(isAndroid = false)
        )
        assertTrue(BugseeOptionsSpec.manifestEnumEntries.isNotEmpty())
    }
}

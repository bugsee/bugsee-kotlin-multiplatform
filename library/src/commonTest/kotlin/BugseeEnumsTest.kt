package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BugseeEnumsTest {

    // BugseeReportType Tests
    @Test
    fun `test BugseeReportType enum values`() {
        // Test that all report types exist
        assertNotNull(BugseeReportType.Bug)
        assertNotNull(BugseeReportType.Error)
        assertNotNull(BugseeReportType.Crash)
    }

    // BugseeNetworkEventStage Tests
    @Test
    fun `test BugseeNetworkEventStage enum values`() {
        // Test that all network event stages exist
        assertNotNull(BugseeNetworkEventStage.Before)
        assertNotNull(BugseeNetworkEventStage.Complete)
        assertNotNull(BugseeNetworkEventStage.Redirect)
        assertNotNull(BugseeNetworkEventStage.Errors)
        assertNotNull(BugseeNetworkEventStage.WebSocket)
    }

    // BugseeVideoMode Tests
    @Test
    fun `test BugseeVideoMode enum values`() {
        // Test that all video modes exist
        assertNotNull(BugseeVideoMode.None)
        assertNotNull(BugseeVideoMode.V1)
        assertNotNull(BugseeVideoMode.V2)
        assertNotNull(BugseeVideoMode.V3)
        assertNotNull(BugseeVideoMode.V4)
    }

    // BugseeVideoQuality Tests
    @Test
    fun `test BugseeVideoQuality enum values`() {
        // Test that all video qualities exist
        assertNotNull(BugseeVideoQuality.Default)
        assertNotNull(BugseeVideoQuality.Medium)
        assertNotNull(BugseeVideoQuality.High)
    }

    // BugseeLifecycleEvent Tests
    @Test
    fun `test BugseeLifecycleEvent enum values`() {
        // Test that all lifecycle events exist
        assertNotNull(BugseeLifecycleEvent.Launched)
        assertNotNull(BugseeLifecycleEvent.Started)
        assertNotNull(BugseeLifecycleEvent.Stopped)
        assertNotNull(BugseeLifecycleEvent.Resumed)
        assertNotNull(BugseeLifecycleEvent.Paused)
    }

    // BugseeReportFields Tests
    @Test
    fun `test BugseeReportFields creation`() {
        val fields = BugseeReportFields("Test Summary", "Test Description", BugseeSeverity.High, listOf("test"))
        
        // Test that BugseeReportFields can be created
        assertNotNull(fields)
        assertEquals("Test Summary", fields.summary)
        assertEquals("Test Description", fields.description)
        assertEquals(BugseeSeverity.High, fields.severity)
        assertEquals(listOf("test"), fields.labels)
    }

    // BugseeExtendedReport Tests
    @Test
    fun `test BugseeExtendedReport creation`() {
        // Note: BugseeExtendedReport is an expect class, so we can't instantiate it in common tests
        // This test verifies that the class exists and is accessible
        assertNotNull(BugseeExtendedReport::class)
    }

    // BugseeLogEvent Tests
    @Test
    fun `test BugseeLogEvent creation`() {
        // Note: BugseeLogEvent is an expect class, so we can't instantiate it in common tests
        // This test verifies that the class exists and is accessible
        assertNotNull(BugseeLogEvent::class)
    }

    // BugseeNetworkEvent Tests
    @Test
    fun `test BugseeNetworkEvent creation`() {
        // Note: BugseeNetworkEvent is an expect class, so we can't instantiate it in common tests
        // This test verifies that the class exists and is accessible
        assertNotNull(BugseeNetworkEvent::class)
    }

    // BugseeAppearance Tests
    @Test
    fun `test BugseeAppearance creation`() {
        // Note: BugseeAppearance is an expect class, so we can't instantiate it in common tests
        // This test verifies that the class exists and is accessible
        assertNotNull(BugseeAppearance::class)
    }

    // BugseeLaunchOptionsAndroid Tests
    @Test
    fun `test BugseeLaunchOptionsAndroid creation`() {
        // Note: BugseeLaunchOptionsAndroid is an expect class, so we can't instantiate it in common tests
        // This test verifies that the class exists and is accessible
        assertNotNull(BugseeLaunchOptionsAndroid::class)
    }

    // BugseeLaunchOptionsIos Tests
    @Test
    fun `test BugseeLaunchOptionsIos creation`() {
        // Note: BugseeLaunchOptionsIos is an expect class, so we can't instantiate it in common tests
        // This test verifies that the class exists and is accessible
        assertNotNull(BugseeLaunchOptionsIos::class)
    }

    // Type alias tests
    @Test
    fun `test type aliases are accessible`() {
        // Test that type aliases are accessible
        assertNotNull(EventHandler::class)
        assertNotNull(TransformHandler::class)
        assertNotNull(ProducerHandler::class)
        assertNotNull(ProducerArgHandler::class)
        
        assertNotNull(BugseeFeedbackEventListener::class)
        assertNotNull(BugseeReportFieldsFiller::class)
        assertNotNull(BugseeReportFieldsFilter::class)
        assertNotNull(BugseeNetworkFilter::class)
        assertNotNull(BugseeLogFilter::class)
        assertNotNull(BugseeLifecycleEventListener::class)
        assertNotNull(BugseeAttachmentsProvider::class)
        assertNotNull(BugseeExtendedReportProvider::class)
    }

    // Test enum value consistency
    @Test
    fun `test enum value consistency`() {
        // Test that enum values are consistent across different access methods
        assertEquals(BugseeSeverity.VeryLow.getLevel(), 1)
        assertEquals(BugseeSeverity.Medium.getLevel(), 2)
        assertEquals(BugseeSeverity.High.getLevel(), 3)
        assertEquals(BugseeSeverity.Critical.getLevel(), 4)
        assertEquals(BugseeSeverity.Blocker.getLevel(), 5)
        
        assertEquals(BugseeLogLevel.Error.getLevel(), 1)
        assertEquals(BugseeLogLevel.Warning.getLevel(), 2)
        assertEquals(BugseeLogLevel.Info.getLevel(), 3)
        assertEquals(BugseeLogLevel.Debug.getLevel(), 4)
        assertEquals(BugseeLogLevel.Verbose.getLevel(), 5)
        
        assertEquals(BugseeFrameRate.Low.getIntValue(), 1)
        assertEquals(BugseeFrameRate.Medium.getIntValue(), 2)
        assertEquals(BugseeFrameRate.High.getIntValue(), 3)
    }

    // Test enum round-trip conversion
    @Test
    fun `test enum round-trip conversion`() {
        // Test that enum values can be converted back and forth
        val severity = BugseeSeverity.High
        val level = severity.getLevel()
        val convertedSeverity = BugseeSeverity.fromLevel(level)
        assertEquals(severity, convertedSeverity)
        
        val logLevel = BugseeLogLevel.Debug
        val logLevelValue = logLevel.getLevel()
        val convertedLogLevel = logLevel.fromLevel(logLevelValue)
        assertEquals(logLevel, convertedLogLevel)
        
        val frameRate = BugseeFrameRate.Medium
        val frameRateValue = frameRate.getIntValue()
        val convertedFrameRate = BugseeFrameRate.fromIntValue(frameRateValue)
        assertEquals(frameRate, convertedFrameRate)
    }

    // Test enum edge cases
    @Test
    fun `test enum edge cases`() {
        // Test edge cases for enum conversions
        assertEquals(BugseeSeverity.Medium, BugseeSeverity.fromLevel(Int.MAX_VALUE))
        assertEquals(BugseeSeverity.Medium, BugseeSeverity.fromLevel(Int.MIN_VALUE))
        
        val logLevel = BugseeLogLevel.Error
        assertEquals(BugseeLogLevel.Info, logLevel.fromLevel(Int.MAX_VALUE))
        assertEquals(BugseeLogLevel.Info, logLevel.fromLevel(Int.MIN_VALUE))
        
        assertEquals(BugseeFrameRate.High, BugseeFrameRate.fromIntValue(Int.MAX_VALUE))
        assertEquals(BugseeFrameRate.High, BugseeFrameRate.fromIntValue(Int.MIN_VALUE))
    }

    // BugseeReportType string conversion
    @Test
    fun `test BugseeReportType fromString - valid values`() {
        assertEquals(BugseeReportType.Bug, BugseeReportType.fromString("bug"))
        assertEquals(BugseeReportType.Error, BugseeReportType.fromString("error"))
        assertEquals(BugseeReportType.Crash, BugseeReportType.fromString("crash"))
    }

    @Test
    fun `test BugseeReportType fromString - unknown defaults to Bug`() {
        assertEquals(BugseeReportType.Bug, BugseeReportType.fromString("unknown"))
        assertEquals(BugseeReportType.Bug, BugseeReportType.fromString(""))
        assertEquals(BugseeReportType.Bug, BugseeReportType.fromString("BUG"))
    }

    @Test
    fun `test BugseeReportType fromString - custom default`() {
        assertEquals(BugseeReportType.Crash, BugseeReportType.fromString("invalid", BugseeReportType.Crash))
        assertEquals(BugseeReportType.Error, BugseeReportType.fromString("invalid", BugseeReportType.Error))
    }

    @Test
    fun `test BugseeReportType toStringValue round trip`() {
        for (type in BugseeReportType.entries) {
            val str = type.toStringValue()
            val roundTrip = BugseeReportType.fromString(str)
            assertEquals(type, roundTrip, "Round trip failed for $type")
        }
    }

    // BugseeNetworkEventStage string conversion
    @Test
    fun `test BugseeNetworkEventStage fromString - valid values`() {
        assertEquals(BugseeNetworkEventStage.Before, BugseeNetworkEventStage.fromString("before"))
        assertEquals(BugseeNetworkEventStage.Complete, BugseeNetworkEventStage.fromString("complete"))
        assertEquals(BugseeNetworkEventStage.Cancel, BugseeNetworkEventStage.fromString("cancel"))
        assertEquals(BugseeNetworkEventStage.Redirect, BugseeNetworkEventStage.fromString("redirect"))
        assertEquals(BugseeNetworkEventStage.Errors, BugseeNetworkEventStage.fromString("error"))
        assertEquals(BugseeNetworkEventStage.WebSocket, BugseeNetworkEventStage.fromString("ws"))
    }

    @Test
    fun `test BugseeNetworkEventStage fromString - unknown defaults to Before`() {
        assertEquals(BugseeNetworkEventStage.Before, BugseeNetworkEventStage.fromString("unknown"))
        assertEquals(BugseeNetworkEventStage.Before, BugseeNetworkEventStage.fromString(""))
        assertEquals(BugseeNetworkEventStage.Before, BugseeNetworkEventStage.fromString(null))
        assertEquals(BugseeNetworkEventStage.Before, BugseeNetworkEventStage.fromString("BEFORE"))
    }

    @Test
    fun `test BugseeNetworkEventStage getStringValue round trip`() {
        for (stage in BugseeNetworkEventStage.entries) {
            val str = stage.getStringValue()
            val roundTrip = BugseeNetworkEventStage.fromString(str)
            assertEquals(stage, roundTrip, "Round trip failed for $stage")
        }
    }

    @Test
    fun `test BugseeSeverity getLevelLong returns correct ULong`() {
        assertEquals(1uL, BugseeSeverity.VeryLow.getLevelLong())
        assertEquals(2uL, BugseeSeverity.Medium.getLevelLong())
        assertEquals(3uL, BugseeSeverity.High.getLevelLong())
        assertEquals(4uL, BugseeSeverity.Critical.getLevelLong())
        assertEquals(5uL, BugseeSeverity.Blocker.getLevelLong())
    }

    // Test enum with custom defaults
    @Test
    fun `test enum with custom defaults`() {
        // Test enum conversion with custom default values
        assertEquals(BugseeSeverity.Critical, BugseeSeverity.fromLevel(999, BugseeSeverity.Critical))
        assertEquals(BugseeSeverity.Blocker, BugseeSeverity.fromLevel(-999, BugseeSeverity.Blocker))
        
        val logLevel = BugseeLogLevel.Error
        assertEquals(BugseeLogLevel.Warning, logLevel.fromLevel(999, BugseeLogLevel.Warning))
        assertEquals(BugseeLogLevel.Debug, logLevel.fromLevel(-999, BugseeLogLevel.Debug))
        
        assertEquals(BugseeFrameRate.Low, BugseeFrameRate.fromIntValue(999, BugseeFrameRate.Low))
        assertEquals(BugseeFrameRate.Medium, BugseeFrameRate.fromIntValue(-999, BugseeFrameRate.Medium))
    }
}

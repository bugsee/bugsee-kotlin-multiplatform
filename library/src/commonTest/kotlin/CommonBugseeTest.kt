package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommonBugseeTest {

    // BugseeSeverity Tests
    @Test
    fun `test BugseeSeverity enum values and levels`() {
        assertEquals(1, BugseeSeverity.VeryLow.getLevel())
        assertEquals(2, BugseeSeverity.Medium.getLevel())
        assertEquals(3, BugseeSeverity.High.getLevel())
        assertEquals(4, BugseeSeverity.Critical.getLevel())
        assertEquals(5, BugseeSeverity.Blocker.getLevel())
    }

    @Test
    fun `test BugseeSeverity fromLevel with valid values`() {
        assertEquals(BugseeSeverity.VeryLow, BugseeSeverity.fromLevel(1))
        assertEquals(BugseeSeverity.Medium, BugseeSeverity.fromLevel(2))
        assertEquals(BugseeSeverity.High, BugseeSeverity.fromLevel(3))
        assertEquals(BugseeSeverity.Critical, BugseeSeverity.fromLevel(4))
        assertEquals(BugseeSeverity.Blocker, BugseeSeverity.fromLevel(5))
    }

    @Test
    fun `test BugseeSeverity fromLevel with invalid values`() {
        assertEquals(BugseeSeverity.Medium, BugseeSeverity.fromLevel(0))
        assertEquals(BugseeSeverity.Medium, BugseeSeverity.fromLevel(6))
        assertEquals(BugseeSeverity.Medium, BugseeSeverity.fromLevel(-1))
        assertEquals(BugseeSeverity.Medium, BugseeSeverity.fromLevel(null))
    }

    @Test
    fun `test BugseeSeverity fromLevel with custom default`() {
        assertEquals(BugseeSeverity.High, BugseeSeverity.fromLevel(0, BugseeSeverity.High))
        assertEquals(BugseeSeverity.Critical, BugseeSeverity.fromLevel(6, BugseeSeverity.Critical))
    }

    @Test
    fun `test BugseeSeverity getLevelLong`() {
        assertEquals(1UL, BugseeSeverity.VeryLow.getLevelLong())
        assertEquals(5UL, BugseeSeverity.Blocker.getLevelLong())
    }

    // BugseeLogLevel Tests
    @Test
    fun `test BugseeLogLevel enum values and levels`() {
        assertEquals(1, BugseeLogLevel.Error.getLevel())
        assertEquals(2, BugseeLogLevel.Warning.getLevel())
        assertEquals(3, BugseeLogLevel.Info.getLevel())
        assertEquals(4, BugseeLogLevel.Debug.getLevel())
        assertEquals(5, BugseeLogLevel.Verbose.getLevel())
    }

    @Test
    fun `test BugseeLogLevel fromLevel with valid values`() {
        val logLevel = BugseeLogLevel.Error
        assertEquals(BugseeLogLevel.Error, logLevel.fromLevel(1))
        assertEquals(BugseeLogLevel.Warning, logLevel.fromLevel(2))
        assertEquals(BugseeLogLevel.Info, logLevel.fromLevel(3))
        assertEquals(BugseeLogLevel.Debug, logLevel.fromLevel(4))
        assertEquals(BugseeLogLevel.Verbose, logLevel.fromLevel(5))
    }

    @Test
    fun `test BugseeLogLevel fromLevel with invalid values`() {
        val logLevel = BugseeLogLevel.Error
        assertEquals(BugseeLogLevel.Info, logLevel.fromLevel(0))
        assertEquals(BugseeLogLevel.Info, logLevel.fromLevel(6))
        assertEquals(BugseeLogLevel.Info, logLevel.fromLevel(-1))
    }

    @Test
    fun `test BugseeLogLevel fromLevel with custom default`() {
        val logLevel = BugseeLogLevel.Error
        assertEquals(BugseeLogLevel.Error, logLevel.fromLevel(0, BugseeLogLevel.Error))
        assertEquals(BugseeLogLevel.Warning, logLevel.fromLevel(6, BugseeLogLevel.Warning))
    }

    @Test
    fun `test BugseeLogLevel getLevelLong`() {
        assertEquals(1UL, BugseeLogLevel.Error.getLevelLong())
        assertEquals(5UL, BugseeLogLevel.Verbose.getLevelLong())
    }

    // BugseeFrameRate Tests
    @Test
    fun `test BugseeFrameRate enum values`() {
        assertEquals(1, BugseeFrameRate.Low.getIntValue())
        assertEquals(2, BugseeFrameRate.Medium.getIntValue())
        assertEquals(3, BugseeFrameRate.High.getIntValue())
    }

    @Test
    fun `test BugseeFrameRate fromIntValue with valid values`() {
        assertEquals(BugseeFrameRate.Low, BugseeFrameRate.fromIntValue(1))
        assertEquals(BugseeFrameRate.Medium, BugseeFrameRate.fromIntValue(2))
        assertEquals(BugseeFrameRate.High, BugseeFrameRate.fromIntValue(3))
    }

    @Test
    fun `test BugseeFrameRate fromIntValue with invalid values`() {
        assertEquals(BugseeFrameRate.High, BugseeFrameRate.fromIntValue(0))
        assertEquals(BugseeFrameRate.High, BugseeFrameRate.fromIntValue(4))
        assertEquals(BugseeFrameRate.High, BugseeFrameRate.fromIntValue(-1))
        assertEquals(BugseeFrameRate.High, BugseeFrameRate.fromIntValue(null))
    }

    @Test
    fun `test BugseeFrameRate fromIntValue with custom default`() {
        assertEquals(BugseeFrameRate.Low, BugseeFrameRate.fromIntValue(0, BugseeFrameRate.Low))
        assertEquals(BugseeFrameRate.Medium, BugseeFrameRate.fromIntValue(4, BugseeFrameRate.Medium))
    }

    // BugseeSecureRectangle Tests
    @Test
    fun `test BugseeSecureRectangle creation`() {
        val rect = BugseeSecureRectangle(10.0, 20.0, 100.0, 200.0)
        assertEquals(10.0, rect.x)
        assertEquals(20.0, rect.y)
        assertEquals(100.0, rect.width)
        assertEquals(200.0, rect.height)
    }

    @Test
    fun `test BugseeSecureRectangle with zero values`() {
        val rect = BugseeSecureRectangle(0.0, 0.0, 0.0, 0.0)
        assertEquals(0.0, rect.x)
        assertEquals(0.0, rect.y)
        assertEquals(0.0, rect.width)
        assertEquals(0.0, rect.height)
    }

    @Test
    fun `test BugseeSecureRectangle with negative values`() {
        val rect = BugseeSecureRectangle(-10.0, -20.0, 100.0, 200.0)
        assertEquals(-10.0, rect.x)
        assertEquals(-20.0, rect.y)
        assertEquals(100.0, rect.width)
        assertEquals(200.0, rect.height)
    }

    // BugseeAttachment Tests
    @Test
    fun `test BugseeAttachment create with data`() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val attachment = BugseeAttachment.create("test.txt", data)
        
        assertEquals("test.txt", attachment.name)
        assertNull(attachment.filePath)
        assertNotNull(attachment.data)
        assertEquals(data.size, attachment.data?.size)
        assertTrue(data.contentEquals(attachment.data))
    }

    @Test
    fun `test BugseeAttachment create with file path`() {
        val attachment = BugseeAttachment.create("test.txt", "/path/to/file.txt")

        assertEquals("test.txt", attachment.name)
        assertEquals("/path/to/file.txt", attachment.filePath)
        assertNull(attachment.data)
    }

    @Test
    fun `test BugseeAttachment create with file URL path`() {
        val attachment = BugseeAttachment.create("test", "file:///path/to/file.txt")
        assertEquals("test", attachment.name)
        assertEquals("file:///path/to/file.txt", attachment.filePath)
        assertNull(attachment.data)
    }

    @Test
    fun `test BugseeAttachment create with relative path`() {
        val attachment = BugseeAttachment.create("test", "files/sample.json")
        assertEquals("test", attachment.name)
        assertEquals("files/sample.json", attachment.filePath)
        assertNull(attachment.data)
    }

    @Test
    fun `test BugseeAttachment create with empty data`() {
        val data = byteArrayOf()
        val attachment = BugseeAttachment.create("empty.txt", data)
        
        assertEquals("empty.txt", attachment.name)
        assertNull(attachment.filePath)
        assertNotNull(attachment.data)
        assertEquals(0, attachment.data?.size)
    }

    @Test
    fun `test BugseeAttachment create with empty file path`() {
        val attachment = BugseeAttachment.create("test.txt", "")
        
        assertEquals("test.txt", attachment.name)
        assertEquals("", attachment.filePath)
        assertNull(attachment.data)
    }

    // BugseeReport Tests
    @Test
    fun `test BugseeReport creation`() {
        val labels = listOf("bug", "critical")
        val report = BugseeReport(BugseeReportType.Bug, BugseeSeverity.High, labels)
        
        assertEquals(BugseeReportType.Bug, report.type)
        assertEquals(BugseeSeverity.High, report.severity)
        assertEquals(labels, report.labels)
    }

    @Test
    fun `test BugseeReport creation with null labels`() {
        val report = BugseeReport(BugseeReportType.Crash, BugseeSeverity.Critical, null)
        
        assertEquals(BugseeReportType.Crash, report.type)
        assertEquals(BugseeSeverity.Critical, report.severity)
        assertNull(report.labels)
    }

    @Test
    fun `test BugseeReport creation with empty labels`() {
        val report = BugseeReport(BugseeReportType.Error, BugseeSeverity.Medium, emptyList())
        
        assertEquals(BugseeReportType.Error, report.type)
        assertEquals(BugseeSeverity.Medium, report.severity)
        assertTrue(report.labels?.isEmpty() ?: false)
    }

    // BugseeExceptionLoggingOptions Tests
    @Test
    fun `test BugseeExceptionLoggingOptions default values`() {
        val options = BugseeExceptionLoggingOptions()
        
        assertNull(options.exceptionDomain)
        assertTrue(options.includeVideo)
        assertNull(options.labels)
        assertNotNull(options.rules)
    }

    @Test
    fun `test BugseeExceptionLoggingOptions with values`() {
        val options = BugseeExceptionLoggingOptions()
        options.exceptionDomain = "test.domain"
        options.includeVideo = false
        options.labels = arrayListOf("error", "critical")
        
        assertEquals("test.domain", options.exceptionDomain)
        assertFalse(options.includeVideo)
        assertEquals(2, options.labels?.size)
        assertEquals("error", options.labels?.get(0))
        assertEquals("critical", options.labels?.get(1))
    }

    @Test
    fun `test BugseeExceptionLoggingOptions MergingRules`() {
        val options = BugseeExceptionLoggingOptions()
        val rules = options.rules
        
        assertEquals(0, rules.skipFrames)
        
        rules.skipFrames = 5
        assertEquals(5, rules.skipFrames)
        
        rules.setCustomOption("testKey", "testValue")
        assertEquals("testValue", rules.getCustomOption("testKey"))
        
        val map = rules.toMap()
        assertEquals(2, map.size)
        assertEquals(5, map["skipFrames"])
        assertEquals("testValue", map["testKey"])
    }

    @Test
    fun `test BugseeExceptionLoggingOptions MergingRules with null values`() {
        val options = BugseeExceptionLoggingOptions()
        val rules = options.rules
        
        rules.setCustomOption("nullKey", null)
        assertNull(rules.getCustomOption("nullKey"))
        
        val map = rules.toMap()
        assertTrue(map.containsKey("nullKey"))
        assertNull(map["nullKey"])
    }
}
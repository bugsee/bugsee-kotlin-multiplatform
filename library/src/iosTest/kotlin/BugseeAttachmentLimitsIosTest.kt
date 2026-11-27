package com.bugsee.kmp

import com.bugsee.kmp.BugseeIOSUtils.Companion.MAX_ATTACHMENT_BYTES
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Foundation.writeToFile
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * The iOS SDK drops an attachment over `MAX_SIZE_ATTACHMENT` (3 MB) or an empty one with only an
 * internal log, so the wrapper skips such attachments itself, with a warning, and checks the size
 * of a file before reading it.
 */
class BugseeAttachmentLimitsIosTest {
    private val files = mutableListOf<String>()

    @AfterTest
    fun deleteFiles() {
        files.forEach { NSFileManager.defaultManager.removeItemAtPath(it, null) }
    }

    @Test
    fun `the limit is the SDK's 3 MB`() {
        assertEquals(3L * 1024 * 1024, MAX_ATTACHMENT_BYTES)
    }

    @Test
    fun `fileSize gives the size of a file and null when there is none`() {
        val path = writeFile(MAX_ATTACHMENT_BYTES + 1)

        assertEquals(MAX_ATTACHMENT_BYTES + 1, BugseeIOSUtils.fileSize(path))
        assertNull(BugseeIOSUtils.fileSize(path + ".missing"))
    }

    @Test
    fun `data of exactly the limit is converted`() {
        val native = assertNotNull(BugseeIOSUtils.convertAttachment(dataAttachment(MAX_ATTACHMENT_BYTES)))
        assertEquals(MAX_ATTACHMENT_BYTES.toULong(), native.data.length)
    }

    @Test
    fun `data over the limit is not converted`() {
        assertNull(BugseeIOSUtils.convertAttachment(dataAttachment(MAX_ATTACHMENT_BYTES + 1)))
    }

    @Test
    fun `a file of exactly the limit is converted with its file name`() {
        val path = writeFile(MAX_ATTACHMENT_BYTES)

        val native = assertNotNull(BugseeIOSUtils.convertAttachment(BugseeAttachment.create("log", path)))

        assertEquals("log", native.name)
        assertEquals(path.substringAfterLast('/'), native.filename)
        assertEquals(MAX_ATTACHMENT_BYTES.toULong(), native.data.length)
    }

    @Test
    fun `a file over the limit is not converted`() {
        val path = writeFile(MAX_ATTACHMENT_BYTES + 1)

        assertNull(BugseeIOSUtils.convertAttachment(BugseeAttachment.create("log", path)))
    }

    @Test
    fun `a file URL over the limit is not converted`() {
        val path = writeFile(MAX_ATTACHMENT_BYTES + 1)
        val url = assertNotNull(NSURL.fileURLWithPath(path).absoluteString)

        assertNull(BugseeIOSUtils.convertAttachment(BugseeAttachment.create("log", url)))
    }

    @Test
    fun `a file URL within the limit is converted`() {
        val path = writeFile(10)
        val url = assertNotNull(NSURL.fileURLWithPath(path).absoluteString)

        val native = assertNotNull(BugseeIOSUtils.convertAttachment(BugseeAttachment.create("log", url)))

        assertEquals(10uL, native.data.length)
    }

    @Test
    fun `an empty file is not converted`() {
        val path = writeFile(0)

        assertNull(BugseeIOSUtils.convertAttachment(BugseeAttachment.create("log", path)))
    }

    @Test
    fun `a missing file is not converted`() {
        val path = NSTemporaryDirectory() + "missing-" + NSUUID().UUIDString + ".log"

        assertNull(BugseeIOSUtils.convertAttachment(BugseeAttachment.create("log", path)))
    }

    @Test
    fun `an empty file path is not converted`() {
        assertNull(BugseeIOSUtils.convertAttachment(BugseeAttachment.create("log", "")))
    }

    private fun dataAttachment(size: Long) = BugseeAttachment.create("blob", ByteArray(size.toInt()))

    @OptIn(BetaInteropApi::class)
    private fun writeFile(size: Long): String {
        val path = NSTemporaryDirectory() + "attachment-" + NSUUID().UUIDString + ".log"
        val bytes = ByteArray(size.toInt())
        val data = if (size == 0L) {
            NSData()
        } else {
            bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = size.toULong()) }
        }
        check(data.writeToFile(path, atomically = true)) { "cannot write $path" }
        files += path
        return path
    }
}

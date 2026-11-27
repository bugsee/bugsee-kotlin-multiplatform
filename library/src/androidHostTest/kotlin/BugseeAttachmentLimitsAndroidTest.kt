package com.bugsee.kmp

import android.net.Uri
import com.bugsee.kmp.internal.MAX_ATTACHMENT_BYTES
import com.bugsee.library.contracts.reporting.Report
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The native SDKs keep an attachment of at most 3 MB (bugsee/specs#86), so the wrapper does not
 * pass a larger one on, and does not read more of a URI or an asset than that to find out.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeAttachmentLimitsAndroidTest {
    private val limit = MAX_ATTACHMENT_BYTES.toInt()
    private val files = mutableListOf<File>()
    private var savedContext = applicationContext

    @Before
    fun setContext() {
        savedContext = applicationContext
        applicationContext = RuntimeEnvironment.getApplication()
    }

    @After
    fun restore() {
        applicationContext = savedContext
        files.forEach { it.delete() }
    }

    @Test
    fun `the limit is 3 MB`() {
        assertEquals(3L * 1024 * 1024, MAX_ATTACHMENT_BYTES)
    }

    @Test
    fun `data of exactly the limit is added`() {
        val report = mock(Report::class.java)
        val data = ByteArray(limit)

        BugseeAndroidUtils.addAttachmentToReport(BugseeAttachment.create("blob", data), report)

        verify(report).addAttachment(data, "blob", null)
    }

    @Test
    fun `data over the limit is not added and a warning is logged`() {
        val report = mock(Report::class.java)

        BugseeAndroidUtils.addAttachmentToReport(BugseeAttachment.create("blob", ByteArray(limit + 1)), report)

        verify(report, never()).addAttachment(any(ByteArray::class.java), anyString(), isNull())
        assertWarned("attachment 'blob' is ${limit + 1} bytes, over the limit of $limit bytes, so it is not added")
    }

    @Test
    fun `a file path of exactly the limit is passed to the SDK as a file`() {
        val report = mock(Report::class.java)
        val file = writeFile(limit)

        BugseeAndroidUtils.addAttachmentToReport(BugseeAttachment.create("log", file.path), report)

        verify(report).addAttachment(eq(file), eq("log"), isNull(), eq(false))
    }

    @Test
    fun `a file path over the limit is not added`() {
        val report = mock(Report::class.java)
        val file = writeFile(limit + 1)

        BugseeAndroidUtils.addAttachmentToReport(BugseeAttachment.create("log", file.path), report)

        verify(report, never()).addAttachment(any(File::class.java), anyString(), isNull(), anyBoolean())
        assertWarned("attachment 'log' is ${limit + 1} bytes, over the limit of $limit bytes, so it is not added")
    }

    @Test
    fun `a file URI of exactly the limit is read and added`() {
        val report = mock(Report::class.java)
        val file = writeFile(limit)

        BugseeAndroidUtils.addAttachmentToReport(BugseeAttachment.create("log", Uri.fromFile(file).toString()), report)

        val captor = ArgumentCaptor.forClass(ByteArray::class.java)
        verify(report).addAttachment(captor.capture(), eq("log"), isNull())
        assertEquals(limit, captor.value.size)
    }

    @Test
    fun `a file URI over the limit is not read`() {
        val report = mock(Report::class.java)
        val file = writeFile(limit + 1)

        BugseeAndroidUtils.addAttachmentToReport(BugseeAttachment.create("log", Uri.fromFile(file).toString()), report)

        verify(report, never()).addAttachment(any(ByteArray::class.java), anyString(), isNull())
        // The size came from the descriptor, before any read: the message gives it.
        assertWarned("attachment 'log' is ${limit + 1} bytes, over the limit of $limit bytes, so it is not added")
    }

    @Test
    fun `a stream within the limit is read whole`() {
        val bytes = assertNotNull(BugseeAndroidUtils.readWithinLimit(ByteArrayInputStream(ByteArray(limit)), "s"))
        assertEquals(limit, bytes.size)
    }

    @Test
    fun `a stream over the limit is not read past it`() {
        val stream = CountingStream(10 * limit)

        assertNull(BugseeAndroidUtils.readWithinLimit(stream, "s"))

        assertTrue(stream.read in (limit + 1)..(limit + 64 * 1024), "read ${stream.read} bytes")
        assertWarned("attachment 's' is over the limit of $limit bytes, so it is not added")
    }

    private fun assertWarned(message: String) {
        val warnings = ShadowLog.getLogsForTag("BugseeAndroidUtils").map { it.msg }
        assertTrue(message in warnings, "no '$message' in $warnings")
    }

    private fun writeFile(size: Int): File {
        val file = File.createTempFile("attachment", ".log")
        file.writeBytes(ByteArray(size))
        files += file
        return file
    }

    /** [size] zero bytes, counting how many were read. */
    private class CountingStream(private val size: Int) : InputStream() {
        var read = 0

        override fun read(): Int = if (read < size) 0.also { read++ } else -1

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (read >= size) return -1
            val count = minOf(len, size - read)
            read += count
            return count
        }
    }
}

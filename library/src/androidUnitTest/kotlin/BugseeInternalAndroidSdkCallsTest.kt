package com.bugsee.kmp.internal

import com.bugsee.kmp.BugseeExceptionLoggingOptions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mockStatic
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Verifies which native SDK 7 overloads the Android actual calls. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeInternalAndroidSdkCallsTest {

    @Test
    fun `logException with options passes domain and skipFrames`() {
        val exception = IllegalStateException("test")
        val options = BugseeExceptionLoggingOptions().apply {
            exceptionDomain = "com.example.domain"
            rules.skipFrames = 2
            labels = arrayListOf("ignored")
        }

        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            BugseeInternal().logException(exception, options)

            sdk.verify { com.bugsee.library.Bugsee.logException(exception, "com.example.domain", 2) }
        }
    }

    @Test
    fun `logException with default options passes no domain and no skipped frames`() {
        val exception = IllegalStateException("test")

        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            BugseeInternal().logException(exception, BugseeExceptionLoggingOptions())

            sdk.verify { com.bugsee.library.Bugsee.logException(exception, null, 0) }
        }
    }

    @Test
    fun `logException with null options uses the plain overload`() {
        val exception = IllegalStateException("test")

        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            BugseeInternal().logException(exception, null)

            sdk.verify { com.bugsee.library.Bugsee.logException(exception) }
        }
    }

    @Test
    fun `deleteCollectedDataOnDevice deletes intermediate data and report bundles`() {
        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            BugseeInternal().deleteCollectedDataOnDevice(null)

            sdk.verify { com.bugsee.library.Bugsee.deleteCollectedDataOnDevice(eq(true), eq(true), any()) }
        }
    }
}

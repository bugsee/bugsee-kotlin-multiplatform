package com.bugsee.kmp

import com.bugsee.kmp.internal.BugseeInternal
import com.bugsee.library.BugseeEnvironment
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeWrapperAdapterAndroidTest {

    @Test
    fun `the adapter describes this library as the iOS adapter does`() {
        val adapter = BugseeWrapperAdapter()

        assertEquals("kmp", adapter.getWrapperType())
        assertEquals(BuildKonfig.libraryVersion, adapter.getWrapperVersion())
        assertEquals(BuildKonfig.buildChecksum, adapter.getWrapperBuild())
        assertEquals(mapOf("runtime" to KotlinVersion.CURRENT.toString()), adapter.getContext())
    }

    @Test
    fun `a data request is answered at once without data`() {
        val results = mutableListOf<String?>()

        BugseeWrapperAdapter().requestData("vh") { results += it }

        assertEquals(listOf<String?>(null), results)
    }

    // The SDK reports sdk.wrapper from the registered wrapper whatever started the session, so a
    // launch(apiKey) or relaunch() without options carries it too.
    @Test
    fun `BugseeInternal registers its adapter with the SDK`() {
        val bugseeInternal = BugseeInternal()

        assertSame(bugseeInternal.wrapperAdapter, BugseeEnvironment.getInstance().getWrapper())
    }
}

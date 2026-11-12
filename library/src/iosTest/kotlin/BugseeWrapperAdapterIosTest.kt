package com.bugsee.kmp

import com.bugsee.kmp.internal.BugseeInternal
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSClassFromString
import platform.Foundation.NSSelectorFromString
import platform.Foundation.valueForKey
import platform.darwin.NSObject
import swiftPMImport.com.bugsee.library.BGSDataRequestResultCallbackProtocol
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BugseeWrapperAdapterIosTest {

    @Test
    fun `the adapter describes this library as Android's wrapper option does`() {
        val adapter = BugseeWrapperAdapter()

        assertEquals("kmp", adapter.wrapperType())
        assertEquals(BuildKonfig.libraryVersion, adapter.wrapperVersion())
        assertEquals(BuildKonfig.buildChecksum, adapter.wrapperBuild())
        assertEquals(mapOf<Any?, Any?>("runtime" to KotlinVersion.CURRENT.toString()), adapter.context())
    }

    @Test
    fun `a data request is answered at once without data`() {
        val results = mutableListOf<String?>()
        val callback = object : NSObject(), BGSDataRequestResultCallbackProtocol {
            override fun onResult(data: String?) {
                results += data
            }
        }

        BugseeWrapperAdapter().requestDataWithType("vh", callback)

        assertEquals(listOf<String?>(null), results)
    }

    // The SDK has no public getter for the registered wrapper or for what it reports. Both are read
    // here from SDK internals through key-value coding: the BGSBugseeEnvironment singleton keeps the
    // adapter, and +[BGSEnvironment wrapperDictionary] builds the session's sdk.wrapper from it.
    // Each key is checked with respondsToSelector first: valueForKey on an unknown key raises an
    // Objective-C exception, which would end the whole test process instead of failing this test.

    @Test
    fun `BugseeInternal registers its adapter with the SDK`() {
        val bugseeInternal = BugseeInternal()

        val environment = sdkValue(sdkClass("BGSBugseeEnvironment"), "sharedEnvironment") as NSObject
        val registered = sdkValue(environment, "wrapper") as NSObject

        assertTrue(registered.isEqual(bugseeInternal.wrapperAdapter))
    }

    @Test
    fun `the SDK reports the library as sdk wrapper with the runtime under context`() {
        BugseeInternal()

        val wrapper = sdkValue(sdkClass("BGSEnvironment"), "wrapperDictionary") as Map<*, *>

        assertEquals(
            mapOf(
                "type" to "kmp",
                "version" to BuildKonfig.libraryVersion,
                "build" to BuildKonfig.buildChecksum,
                "context" to mapOf("runtime" to KotlinVersion.CURRENT.toString()),
            ),
            wrapper,
        )
    }

    @OptIn(BetaInteropApi::class)
    private fun sdkClass(name: String): NSObject =
        assertNotNull(NSClassFromString(name), "the SDK has no class $name") as NSObject

    private fun sdkValue(receiver: NSObject, key: String): Any {
        assertTrue(receiver.respondsToSelector(NSSelectorFromString(key)), "the SDK has no $key on $receiver")
        return assertNotNull(receiver.valueForKey(key), "$key is nil")
    }
}

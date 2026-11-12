package com.bugsee.kmp

import com.bugsee.kmp.internal.BugseeInternal
import platform.Foundation.NSClassFromString
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

        BugseeWrapperAdapter().requestDataWithType("viewHierarchy", callback)

        assertEquals(listOf<String?>(null), results)
    }

    // The SDK has no public getter for the registered wrapper; it keeps it on its internal
    // BGSBugseeEnvironment singleton, read here through key-value coding. If the SDK renames
    // either, this test fails instead of passing vacuously.
    @Test
    fun `BugseeInternal registers its adapter with the SDK`() {
        val bugseeInternal = BugseeInternal()

        val environmentClass = assertNotNull(NSClassFromString("BGSBugseeEnvironment")) as NSObject
        val environment = assertNotNull(environmentClass.valueForKey("sharedEnvironment")) as NSObject
        val registered = assertNotNull(environment.valueForKey("wrapper")) as NSObject

        // Two Kotlin references to one Objective-C object are not ===; compare the objects.
        assertTrue(registered.isEqual(bugseeInternal.wrapperAdapter))
    }
}

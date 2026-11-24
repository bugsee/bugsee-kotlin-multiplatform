package com.bugsee.kmp

import com.bugsee.kmp.internal.legacyLaunchOptionKeys
import org.junit.Test
import kotlin.test.assertEquals

class LegacyLaunchOptionKeysAndroidTest {

    // The error names the constant: it must be the BugseeOptions constant of the new key.
    @Test
    fun `each new key is named by its BugseeOptions constant`() {
        legacyLaunchOptionKeys.forEach { (key, legacy) ->
            val replacement = legacy.replacement ?: return@forEach
            assertEquals(replacement.key, BugseeOptions::class.java.getField(replacement.name).get(null), key)
        }
    }
}

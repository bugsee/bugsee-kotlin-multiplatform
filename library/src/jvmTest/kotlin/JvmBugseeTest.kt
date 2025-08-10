package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals

class JvmBugseeTest {

    @Test
    public fun `test 3rd element`() {
        assertEquals(5, generateFibi().take(3).last())
    }
}
package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidBugseeTest {

    @Test
    public fun `test 3rd element`() {
        assertEquals(3, generateFibi().take(3).last())
    }
}
package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals

class IosBugseeTest {

    @Test
    public fun `test 3rd element`() {
        assertEquals(7, generateFibi().take(3).last())
    }
}
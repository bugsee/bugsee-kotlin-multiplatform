package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals

class FibiTest {

    @Test
    public fun `test 3rd element`() {
        assertEquals(firstElement + secondElement, generateFibi().take(3).last())
    }
}
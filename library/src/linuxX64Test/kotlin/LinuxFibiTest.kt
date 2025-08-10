package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals

class LinuxFibiTest {

    @Test
    public fun `test 3rd element`() {
        assertEquals(8, generateFibi().take(3).last())
    }
}

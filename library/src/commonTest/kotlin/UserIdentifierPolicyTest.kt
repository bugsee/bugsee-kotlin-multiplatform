package com.bugsee.kmp.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserIdentifierPolicyTest {

    @Test
    fun `an empty or null identifier is set as a clear`() {
        assertNull(UserIdentifierPolicy.forSet(null))
        assertNull(UserIdentifierPolicy.forSet(""))
    }

    @Test
    fun `any other identifier is set as is`() {
        assertEquals("user@example.com", UserIdentifierPolicy.forSet("user@example.com"))
        assertEquals(" ", UserIdentifierPolicy.forSet(" "))
    }

    @Test
    fun `an empty identifier reads back as null`() {
        assertNull(UserIdentifierPolicy.forGet(null))
        assertNull(UserIdentifierPolicy.forGet(""))
        assertEquals("user-42", UserIdentifierPolicy.forGet("user-42"))
    }
}

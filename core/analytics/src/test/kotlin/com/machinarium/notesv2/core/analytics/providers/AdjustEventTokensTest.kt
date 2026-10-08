package com.machinarium.notesv2.core.analytics.providers

import kotlin.test.assertEquals
import org.junit.Test

class AdjustEventTokensTest {

    @Test
    fun `given name-token pairs, then they are parsed`() {
        assertEquals(
            mapOf("note_opened" to "abc123", "purchase" to "def456"),
            parseEventTokens("note_opened:abc123, purchase:def456"),
        )
    }

    @Test
    fun `given malformed or blank entries, then they are ignored`() {
        assertEquals(mapOf("ok" to "t1"), parseEventTokens("ok:t1,,broken,:t2,empty:"))
    }

    @Test
    fun `given an empty property, then no event is mapped`() {
        assertEquals(emptyMap(), parseEventTokens(""))
    }
}

package com.machinarium.notesv2.core.analytics

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.Test

class AnalyticsEventTest {

    @Test
    fun `given a valid name and params, then the event is created`() {
        val event = AnalyticsEvent(name = "note_opened", params = mapOf("source" to "list"))

        assertEquals("note_opened", event.name)
    }

    @Test
    fun `given a name with spaces, then creation fails`() {
        assertFailsWith<IllegalArgumentException> { AnalyticsEvent(name = "note opened") }
    }

    @Test
    fun `given a name longer than 40 characters, then creation fails`() {
        assertFailsWith<IllegalArgumentException> { AnalyticsEvent(name = "a".repeat(41)) }
    }

    @Test
    fun `given more than 25 params, then creation fails`() {
        val params = (1..26).associate { "p$it" to "v" }

        assertFailsWith<IllegalArgumentException> { AnalyticsEvent(name = "too_many", params = params) }
    }
}

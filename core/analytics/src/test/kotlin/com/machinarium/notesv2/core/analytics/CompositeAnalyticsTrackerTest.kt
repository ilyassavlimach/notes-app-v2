package com.machinarium.notesv2.core.analytics

import kotlin.test.assertEquals
import org.junit.Test

class CompositeAnalyticsTrackerTest {

    private val first = RecordingProvider("first")
    private val second = RecordingProvider("second")
    private val event = AnalyticsEvent("note_opened", mapOf("note_id" to "7"))

    @Test
    fun `when tracking, then every provider receives the event`() {
        CompositeAnalyticsTracker(setOf(first, second)).track(event)

        assertEquals(listOf(event), first.events)
        assertEquals(listOf(event), second.events)
    }

    @Test
    fun `given a provider maps the event to null, then only that provider skips it`() {
        val skipping = RecordingProvider("skipping", mapper = { null })

        CompositeAnalyticsTracker(setOf(skipping, first)).track(event)

        assertEquals(emptyList(), skipping.events)
        assertEquals(listOf(event), first.events)
    }

    @Test
    fun `given a provider renames the event, then it receives the mapped event`() {
        val renaming = RecordingProvider("renaming", mapper = { it.copy(name = "open_note") })

        CompositeAnalyticsTracker(setOf(renaming)).track(event)

        assertEquals(listOf("open_note"), renaming.events.map(AnalyticsEvent::name))
    }

    @Test
    fun `given a provider throws, then the other providers still receive the event`() {
        val failing = RecordingProvider("failing", failure = IllegalStateException("sdk not ready"))

        CompositeAnalyticsTracker(setOf(failing, first)).track(event)

        assertEquals(listOf(event), first.events)
    }

    @Test
    fun `when disabled, then nothing is tracked and every provider is disabled`() {
        val tracker = CompositeAnalyticsTracker(setOf(first, second))

        tracker.setEnabled(false)
        tracker.track(event)

        assertEquals(emptyList(), first.events)
        assertEquals(listOf(false), first.enabledCalls)
        assertEquals(listOf(false), second.enabledCalls)
    }

    @Test
    fun `when the user id is set, then every provider gets it`() {
        CompositeAnalyticsTracker(setOf(first, second)).setUserId("u-1")

        assertEquals(listOf<String?>("u-1"), first.userIds)
        assertEquals(listOf<String?>("u-1"), second.userIds)
    }

    @Test
    fun `when initializing, then every provider is started even if one fails`() {
        val failing = RecordingProvider("failing", failure = IllegalStateException("bad key"))

        CompositeAnalyticsTracker(setOf(failing, first)).initializeAll()

        assertEquals(1, first.initializeCount)
    }

    private class RecordingProvider(
        override val name: String,
        private val mapper: (AnalyticsEvent) -> AnalyticsEvent? = { it },
        private val failure: RuntimeException? = null,
    ) : AnalyticsProvider {
        val events = mutableListOf<AnalyticsEvent>()
        val userIds = mutableListOf<String?>()
        val enabledCalls = mutableListOf<Boolean>()
        var initializeCount = 0
            private set

        override fun initialize() {
            failure?.let { throw it }
            initializeCount++
        }

        override fun map(event: AnalyticsEvent): AnalyticsEvent? = mapper(event)

        override fun track(event: AnalyticsEvent) {
            failure?.let { throw it }
            events += event
        }

        override fun setUserId(userId: String?) {
            userIds += userId
        }

        override fun setEnabled(enabled: Boolean) {
            enabledCalls += enabled
        }
    }
}

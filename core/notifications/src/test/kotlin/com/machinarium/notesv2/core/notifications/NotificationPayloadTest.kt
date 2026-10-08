package com.machinarium.notesv2.core.notifications

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class NotificationPayloadTest {

    private val schemes = setOf("https")

    @Test
    fun `given title and an allowed deep link, then both are kept`() {
        val payload = NotificationPayload.from(
            title = "New note",
            body = "Open it",
            data = mapOf(NotificationPayload.KEY_DEEP_LINK to "https://example.com/notes/1"),
            allowedSchemes = schemes,
        )

        assertEquals(NotificationPayload("New note", "Open it", "https://example.com/notes/1"), payload)
    }

    @Test
    fun `given a deep link with a foreign scheme, then the link is dropped`() {
        val payload = NotificationPayload.from(
            title = "Hi",
            body = null,
            data = mapOf(NotificationPayload.KEY_DEEP_LINK to "intent://evil"),
            allowedSchemes = schemes,
        )

        assertNull(payload?.deepLink)
        assertEquals("", payload?.body)
    }

    @Test
    fun `given neither title nor body, then nothing is shown`() {
        assertNull(NotificationPayload.from(title = " ", body = null, data = emptyMap(), allowedSchemes = schemes))
    }
}

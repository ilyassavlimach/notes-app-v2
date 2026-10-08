package com.machinarium.notesv2.core.network.model

import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import org.junit.Test

class NoteDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `given an api payload with userId, when decoded, then unknown fields are ignored`() {
        val payload = """[{"userId":1,"id":7,"title":"Title","body":"Body"}]"""

        val notes = json.decodeFromString<List<NoteDto>>(payload)

        assertEquals(listOf(NoteDto(id = 7, title = "Title", body = "Body")), notes)
    }
}

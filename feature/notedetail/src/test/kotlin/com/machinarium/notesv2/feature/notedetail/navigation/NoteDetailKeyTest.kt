package com.machinarium.notesv2.feature.notedetail.navigation

import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import org.junit.Test

class NoteDetailKeyTest {

    @Test
    fun `when the key is saved and restored, then the note id survives`() {
        val key = NoteDetailKey(noteId = 42)

        assertEquals(key, Json.decodeFromString<NoteDetailKey>(Json.encodeToString(key)))
    }
}

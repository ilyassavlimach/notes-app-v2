package com.machinarium.notesv2.feature.noteeditor.navigation

import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import org.junit.Test

class NoteEditorKeyTest {

    @Test
    fun `when keys are saved and restored, then new and edit keys survive`() {
        listOf(NoteEditorKey(), NoteEditorKey(noteId = 7)).forEach { key ->
            assertEquals(key, Json.decodeFromString<NoteEditorKey>(Json.encodeToString(key)))
        }
    }
}

package com.machinarium.notesv2.feature.noteslist

import com.machinarium.notesv2.core.model.Note
import kotlin.test.assertEquals
import org.junit.Test

class NoteItemUiMapperTest {

    private val mapper = NoteItemUiMapper()

    @Test
    fun `when mapped, then id and title are kept`() {
        val item = mapper.map(Note(id = 4, title = "Title", body = "Body"))

        assertEquals(4L, item.id)
        assertEquals("Title", item.title)
    }

    @Test
    fun `given a multi-line body, when mapped, then the preview is a single line`() {
        val item = mapper.map(Note(id = 1, title = "Title", body = "first line\n second line\nthird"))

        assertEquals("first line second line third", item.preview)
    }
}

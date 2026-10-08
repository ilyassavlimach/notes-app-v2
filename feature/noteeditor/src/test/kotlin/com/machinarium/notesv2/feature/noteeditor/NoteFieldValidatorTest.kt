package com.machinarium.notesv2.feature.noteeditor

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class NoteFieldValidatorTest {

    @Test
    fun `given an empty new note, then there is no error but it can't be saved`() {
        assertNull(NoteFieldValidator.titleError(title = "", body = ""))
        assertEquals(false, NoteFieldValidator.isSavable(title = "", body = ""))
    }

    @Test
    fun `given a body without title, then the title is required`() {
        assertEquals(FieldError.Required, NoteFieldValidator.titleError(title = "  ", body = "Text"))
    }

    @Test
    fun `given fields over their limits, then they are too long and not savable`() {
        val longTitle = "a".repeat(NoteFieldValidator.TITLE_MAX_LENGTH + 1)
        val longBody = "b".repeat(NoteFieldValidator.BODY_MAX_LENGTH + 1)

        assertEquals(FieldError.TooLong, NoteFieldValidator.titleError(longTitle, body = ""))
        assertEquals(FieldError.TooLong, NoteFieldValidator.bodyError(longBody))
        assertEquals(false, NoteFieldValidator.isSavable(title = "Title", body = longBody))
    }

    @Test
    fun `given fields at their limits, then they are savable`() {
        val title = "a".repeat(NoteFieldValidator.TITLE_MAX_LENGTH)
        val body = "b".repeat(NoteFieldValidator.BODY_MAX_LENGTH)

        assertEquals(true, NoteFieldValidator.isSavable(title, body))
    }
}

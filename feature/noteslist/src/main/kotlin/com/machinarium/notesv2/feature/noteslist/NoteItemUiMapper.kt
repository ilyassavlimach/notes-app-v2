package com.machinarium.notesv2.feature.noteslist

import com.machinarium.notesv2.core.model.Note
import javax.inject.Inject

internal class NoteItemUiMapper @Inject constructor() {
    /** The preview is one flowing paragraph; line breaks in the body would waste the two preview lines. */
    fun map(note: Note): NoteItemUi = NoteItemUi(
        id = note.id,
        title = note.title,
        preview = note.body.lines().joinToString(separator = " ") { it.trim() },
    )
}

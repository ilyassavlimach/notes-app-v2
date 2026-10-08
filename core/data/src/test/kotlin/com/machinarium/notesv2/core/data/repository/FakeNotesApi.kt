package com.machinarium.notesv2.core.data.repository

import com.machinarium.notesv2.core.network.NotesApi
import com.machinarium.notesv2.core.network.model.NoteDto

internal class FakeNotesApi : NotesApi {
    var notes: List<NoteDto> = emptyList()
    var failure: Exception? = null
    var callCount = 0
        private set

    override suspend fun getNotes(): List<NoteDto> {
        callCount++
        failure?.let { throw it }
        return notes
    }
}

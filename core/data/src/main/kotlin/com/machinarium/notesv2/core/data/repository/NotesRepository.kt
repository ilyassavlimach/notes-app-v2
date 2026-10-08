package com.machinarium.notesv2.core.data.repository

import com.machinarium.notesv2.core.common.result.AppResult
import com.machinarium.notesv2.core.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Notes for the features. The local database is the source of truth; [refresh] merges the API copy into it. */
interface NotesRepository {
    /** Id of the note deleted last, until a screen offered Undo for it ([consumeRecentlyDeleted]). */
    val recentlyDeleted: StateFlow<Long?>

    fun observeNotes(): Flow<List<Note>>

    /** Emits `null` while no visible note has this id (missing or deleted). */
    fun observeNote(id: Long): Flow<Note?>

    suspend fun refresh(): AppResult<Unit>

    suspend fun deleteNote(id: Long): AppResult<Unit>

    suspend fun restoreNote(id: Long): AppResult<Unit>

    fun consumeRecentlyDeleted()
}

package com.machinarium.notesv2.core.testing.repository

import com.machinarium.notesv2.core.common.result.AppResult
import com.machinarium.notesv2.core.data.repository.NotesRepository
import com.machinarium.notesv2.core.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FakeNotesRepository : NotesRepository {
    private val notes = MutableStateFlow<List<Note>>(emptyList())
    private val deletedNotes = mutableMapOf<Long, Note>()
    private val recentlyDeletedId = MutableStateFlow<Long?>(null)

    override val recentlyDeleted: StateFlow<Long?> = recentlyDeletedId

    /** When set, the next subscriptions to the observe flows fail with it. */
    var observeFailure: Throwable? = null

    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    var refreshCount = 0
        private set

    /** When set, delete and restore fail with it and change nothing. */
    var writeFailure: AppResult.Failure? = null

    val restoredIds = mutableListOf<Long>()

    fun emit(value: List<Note>) {
        notes.value = value
    }

    override fun observeNotes(): Flow<List<Note>> = flow {
        observeFailure?.let { throw it }
        emitAll(notes)
    }

    override fun observeNote(id: Long): Flow<Note?> = observeNotes().map { list -> list.firstOrNull { it.id == id } }

    override suspend fun refresh(): AppResult<Unit> {
        refreshCount++
        return refreshResult
    }

    override suspend fun deleteNote(id: Long): AppResult<Unit> {
        writeFailure?.let { return it }
        val note = notes.value.firstOrNull { it.id == id } ?: return AppResult.Success(Unit)
        deletedNotes[id] = note
        notes.value = notes.value - note
        recentlyDeletedId.value = id
        return AppResult.Success(Unit)
    }

    override suspend fun restoreNote(id: Long): AppResult<Unit> {
        writeFailure?.let { return it }
        restoredIds += id
        deletedNotes.remove(id)?.let { notes.value = notes.value + it }
        return AppResult.Success(Unit)
    }

    override fun consumeRecentlyDeleted() {
        recentlyDeletedId.value = null
    }
}

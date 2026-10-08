package com.machinarium.notesv2.core.data.repository

import com.machinarium.notesv2.core.database.dao.NoteDao
import com.machinarium.notesv2.core.database.model.NoteEntity
import com.machinarium.notesv2.core.database.model.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory DAO; `mergeRemote` is the interface's real default implementation running on top of it. */
internal class FakeNoteDao : NoteDao {
    private val rows = MutableStateFlow<List<NoteEntity>>(emptyList())
    private var nextId = 1L

    var writeFailure: RuntimeException? = null

    val stored: List<NoteEntity> get() = rows.value

    override fun observeVisible(): Flow<List<NoteEntity>> = rows.map { all ->
        all.filterNot(NoteEntity::isDeleted).sortedWith(
            compareByDescending<NoteEntity> {
                it.updatedAt
            }.thenBy { it.id },
        )
    }

    override fun observeVisibleById(id: Long): Flow<NoteEntity?> =
        rows.map { all -> all.firstOrNull { it.id == id && !it.isDeleted } }

    override suspend fun setDeleted(
        id: Long,
        isDeleted: Boolean,
    ): Int {
        writeFailure?.let { throw it }
        val exists = rows.value.any { it.id == id }
        rows.update { all -> all.map { if (it.id == id) it.copy(isDeleted = isDeleted) else it } }
        return if (exists) 1 else 0
    }

    override fun observeHasLocal(): Flow<Boolean> =
        rows.map { all -> all.any { it.syncState == SyncState.LOCAL && !it.isDeleted } }

    override suspend fun insert(note: NoteEntity): Long {
        writeFailure?.let { throw it }
        val row = note.copy(id = nextId++)
        rows.update { it + row }
        return row.id
    }

    override suspend fun updateContent(
        id: Long,
        title: String,
        body: String,
        updatedAt: Long,
    ): Int {
        writeFailure?.let { throw it }
        val target = rows.value.firstOrNull { it.id == id && !it.isDeleted } ?: return 0
        val edited = target.copy(title = title, body = body, updatedAt = updatedAt, syncState = SyncState.LOCAL)
        rows.update { all -> all.map { if (it.id == id) edited else it } }
        return 1
    }

    override suspend fun getRemoteBacked(): List<NoteEntity> = rows.value.filter { it.remoteId != null }

    override suspend fun upsertAll(notes: List<NoteEntity>) {
        writeFailure?.let { throw it }
        rows.update { current ->
            notes.fold(current) { acc, note ->
                val row = if (note.id == 0L) note.copy(id = nextId++) else note
                acc.filterNot { it.id == row.id } + row
            }
        }
    }

    override suspend fun deleteByIds(ids: List<Long>) {
        rows.update { current -> current.filterNot { it.id in ids } }
    }
}

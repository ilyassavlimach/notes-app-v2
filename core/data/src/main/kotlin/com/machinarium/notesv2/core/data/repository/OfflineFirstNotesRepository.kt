package com.machinarium.notesv2.core.data.repository

import android.database.SQLException
import com.machinarium.notesv2.core.common.dispatcher.IoDispatcher
import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.common.result.AppResult
import com.machinarium.notesv2.core.database.dao.NoteDao
import com.machinarium.notesv2.core.database.model.NoteEntity
import com.machinarium.notesv2.core.database.model.SyncState
import com.machinarium.notesv2.core.database.model.toDomain
import com.machinarium.notesv2.core.model.Note
import com.machinarium.notesv2.core.network.NotesApi
import com.machinarium.notesv2.core.network.model.NoteDto
import com.machinarium.notesv2.core.network.safeApiCall
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

// Singleton: recentlyDeleted is shared between the screen that deletes and the one that offers Undo.
@Singleton
internal class OfflineFirstNotesRepository @Inject constructor(
    private val notesApi: NotesApi,
    private val noteDao: NoteDao,
    private val clock: Clock,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : NotesRepository {

    private val recentlyDeletedId = MutableStateFlow<Long?>(null)
    override val recentlyDeleted: StateFlow<Long?> = recentlyDeletedId.asStateFlow()

    override fun observeNotes(): Flow<List<Note>> =
        noteDao.observeVisible().map { entities -> entities.map(NoteEntity::toDomain) }

    override fun observeNote(id: Long): Flow<Note?> = noteDao.observeVisibleById(id).map { it?.toDomain() }

    override fun observeHasUserNotes(): Flow<Boolean> = noteDao.observeHasLocal()

    override suspend fun refresh(): AppResult<Unit> = withContext(ioDispatcher) {
        when (val result = safeApiCall { notesApi.getNotes() }) {
            is AppResult.Success -> databaseCall { noteDao.mergeRemote(result.data.map(NoteDto::toSyncedEntity)) }
            // The database is left untouched, so offline users keep every note.
            is AppResult.Failure -> result
        }
    }

    override suspend fun createNote(
        title: String,
        body: String,
    ): AppResult<Long> = withContext(ioDispatcher) {
        databaseCall {
            noteDao.insert(
                NoteEntity(
                    remoteId = null,
                    title = title,
                    body = body,
                    updatedAt = clock.millis(),
                    syncState = SyncState.LOCAL,
                ),
            )
        }
    }

    override suspend fun updateNote(
        id: Long,
        title: String,
        body: String,
    ): AppResult<Unit> = withContext(ioDispatcher) {
        databaseCall { noteDao.updateContent(id, title, body, updatedAt = clock.millis()) }.requireRowChanged()
    }

    override suspend fun deleteNote(id: Long): AppResult<Unit> = setDeleted(id, isDeleted = true).also { result ->
        if (result is AppResult.Success) recentlyDeletedId.value = id
    }

    override suspend fun restoreNote(id: Long): AppResult<Unit> = setDeleted(id, isDeleted = false)

    override fun consumeRecentlyDeleted() {
        recentlyDeletedId.value = null
    }

    private suspend fun setDeleted(
        id: Long,
        isDeleted: Boolean,
    ): AppResult<Unit> = withContext(ioDispatcher) {
        databaseCall { noteDao.setDeleted(id, isDeleted) }.requireRowChanged()
    }

    private suspend fun <T> databaseCall(block: suspend () -> T): AppResult<T> = try {
        AppResult.Success(block())
    } catch (exception: SQLException) {
        // e.g. disk full: the transaction rolls back, the old rows stay and the UI gets a typed failure.
        AppResult.Failure(AppError.Unknown)
    }
}

private fun NoteDto.toSyncedEntity(): NoteEntity =
    NoteEntity(remoteId = id, title = title, body = body, updatedAt = 0, syncState = SyncState.SYNCED)

/** 0 rows changed means the note is gone (e.g. removed by a refresh or deleted), so the write did nothing. */
private fun AppResult<Int>.requireRowChanged(): AppResult<Unit> = when (this) {
    is AppResult.Success -> if (data > 0) AppResult.Success(Unit) else AppResult.Failure(AppError.Unknown)
    is AppResult.Failure -> this
}

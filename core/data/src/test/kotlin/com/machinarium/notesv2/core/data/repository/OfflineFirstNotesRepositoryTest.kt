package com.machinarium.notesv2.core.data.repository

import android.database.sqlite.SQLiteFullException
import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.common.result.AppResult
import com.machinarium.notesv2.core.database.model.NoteEntity
import com.machinarium.notesv2.core.database.model.SyncState
import com.machinarium.notesv2.core.model.Note
import com.machinarium.notesv2.core.network.model.NoteDto
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Test

class OfflineFirstNotesRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private val notesApi = FakeNotesApi()
    private val noteDao = FakeNoteDao()
    private val repository = OfflineFirstNotesRepository(notesApi, noteDao, testDispatcher)

    @Test
    fun `given stored rows, when observed, then emits visible domain notes`() = runTest(testDispatcher) {
        noteDao.upsertAll(listOf(entity(remoteId = 1, title = "Kept"), entity(remoteId = 2).copy(isDeleted = true)))

        assertEquals(listOf(Note(id = 1, title = "Kept", body = "Body")), repository.observeNotes().first())
    }

    @Test
    fun `given the api returns notes, when refreshed, then they are stored as synced rows`() = runTest(testDispatcher) {
        notesApi.notes = listOf(NoteDto(id = 7, title = "Fresh", body = "Body"))

        val result = repository.refresh()

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(listOf(entity(remoteId = 7, title = "Fresh").copy(id = 1)), noteDao.stored)
    }

    @Test
    fun `given a note the user edited, when refreshed, then the user's copy is kept`() = runTest(testDispatcher) {
        noteDao.upsertAll(listOf(entity(remoteId = 7, title = "Mine").copy(syncState = SyncState.LOCAL)))
        notesApi.notes = listOf(NoteDto(id = 7, title = "Server", body = "Body"))

        repository.refresh()

        assertEquals(listOf("Mine"), noteDao.stored.map(NoteEntity::title))
    }

    @Test
    fun `given no network, when refreshed, then fails with network error and keeps the rows`() =
        runTest(testDispatcher) {
            noteDao.upsertAll(listOf(entity(remoteId = 1)))
            val before = noteDao.stored
            notesApi.failure = IOException("offline")

            val result = repository.refresh()

            assertEquals(AppResult.Failure(AppError.Network), result)
            assertEquals(before, noteDao.stored)
        }

    @Test
    fun `given the database write fails, when refreshed, then fails with unknown error`() = runTest(testDispatcher) {
        notesApi.notes = listOf(NoteDto(id = 1, title = "Fresh", body = "Body"))
        noteDao.writeFailure = SQLiteFullException("disk full")

        val result = repository.refresh()

        assertEquals(AppResult.Failure(AppError.Unknown), result)
    }

    @Test
    fun `given a malformed response, when refreshed, then fails without retrying`() = runTest(testDispatcher) {
        notesApi.failure = SerializationException("bad json")

        val result = repository.refresh()

        assertEquals(AppResult.Failure(AppError.Unknown), result)
        assertEquals(1, notesApi.callCount)
        assertEquals(emptyList(), noteDao.stored)
    }

    @Test
    fun `given a stored note, when observed by id, then emits it until it is deleted`() = runTest(testDispatcher) {
        noteDao.upsertAll(listOf(entity(remoteId = 1, title = "One")))

        assertEquals(Note(id = 1, title = "One", body = "Body"), repository.observeNote(1).first())

        repository.deleteNote(1)
        assertNull(repository.observeNote(1).first())
    }

    @Test
    fun `when a note is deleted, then it is soft deleted and offered for undo`() = runTest(testDispatcher) {
        noteDao.upsertAll(listOf(entity(remoteId = 1)))

        val result = repository.deleteNote(1)

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(listOf(true), noteDao.stored.map(NoteEntity::isDeleted))
        assertEquals(1L, repository.recentlyDeleted.value)
    }

    @Test
    fun `given a deleted note, when restored and consumed, then it is visible and no undo is pending`() =
        runTest(testDispatcher) {
            noteDao.upsertAll(listOf(entity(remoteId = 1)))
            repository.deleteNote(1)

            val result = repository.restoreNote(1)
            repository.consumeRecentlyDeleted()

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(listOf(false), noteDao.stored.map(NoteEntity::isDeleted))
            assertNull(repository.recentlyDeleted.value)
        }

    @Test
    fun `given no such note, when deleted, then fails and offers no undo`() = runTest(testDispatcher) {
        val result = repository.deleteNote(42)

        assertEquals(AppResult.Failure(AppError.Unknown), result)
        assertNull(repository.recentlyDeleted.value)
    }

    @Test
    fun `given the database write fails, when deleted, then fails with unknown error`() = runTest(testDispatcher) {
        noteDao.upsertAll(listOf(entity(remoteId = 1)))
        noteDao.writeFailure = SQLiteFullException("disk full")

        assertEquals(AppResult.Failure(AppError.Unknown), repository.deleteNote(1))
    }

    private fun entity(
        remoteId: Long,
        title: String = "Note $remoteId",
    ) = NoteEntity(remoteId = remoteId, title = title, body = "Body", updatedAt = 0, syncState = SyncState.SYNCED)
}

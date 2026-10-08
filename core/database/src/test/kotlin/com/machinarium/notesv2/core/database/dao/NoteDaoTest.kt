package com.machinarium.notesv2.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.machinarium.notesv2.core.database.NotesDatabase
import com.machinarium.notesv2.core.database.model.NoteEntity
import com.machinarium.notesv2.core.database.model.SyncState
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var database: NotesDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            NotesDatabase::class.java,
        ).allowMainThreadQueries().build()
        noteDao = database.noteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `given user and seeded notes, when observed, then newest user changes come first and deleted are hidden`() =
        runTest {
            noteDao.upsertAll(
                listOf(
                    remote(remoteId = 2),
                    remote(remoteId = 1),
                    local(title = "Older", updatedAt = 10),
                    local(title = "Newer", updatedAt = 20),
                    remote(remoteId = 3).copy(isDeleted = true),
                ),
            )

            val titles = noteDao.observeVisible().first().map(NoteEntity::title)

            assertEquals(listOf("Newer", "Older", "Remote 2", "Remote 1"), titles)
        }

    @Test
    fun `given an empty table, when merged, then every remote note is inserted`() = runTest {
        noteDao.mergeRemote(listOf(remote(remoteId = 1), remote(remoteId = 2)))

        assertEquals(listOf(1L, 2L), noteDao.observeVisible().first().map(NoteEntity::remoteId))
    }

    @Test
    fun `given a synced note, when merged with a new copy, then it is updated in place`() = runTest {
        noteDao.mergeRemote(listOf(remote(remoteId = 1, title = "Old")))
        val id = noteDao.observeVisible().first().single().id

        noteDao.mergeRemote(listOf(remote(remoteId = 1, title = "New")))

        val note = noteDao.observeVisible().first().single()
        assertEquals(id to "New", note.id to note.title)
    }

    @Test
    fun `given an edited seeded note, when merged, then the user's copy is kept`() = runTest {
        noteDao.upsertAll(listOf(remote(remoteId = 1, title = "Mine").copy(syncState = SyncState.LOCAL)))

        noteDao.mergeRemote(listOf(remote(remoteId = 1, title = "Server")))

        assertEquals(listOf("Mine"), noteDao.observeVisible().first().map(NoteEntity::title))
    }

    @Test
    fun `given a deleted seeded note, when merged, then it stays deleted`() = runTest {
        noteDao.upsertAll(listOf(remote(remoteId = 1).copy(isDeleted = true)))

        noteDao.mergeRemote(listOf(remote(remoteId = 1)))

        assertEquals(emptyList(), noteDao.observeVisible().first())
        assertEquals(listOf(true), noteDao.getRemoteBacked().map(NoteEntity::isDeleted))
    }

    @Test
    fun `given a synced note missing from the api, when merged, then only it is removed`() = runTest {
        noteDao.upsertAll(listOf(remote(remoteId = 1), remote(remoteId = 2), local(title = "Mine", updatedAt = 5)))

        noteDao.mergeRemote(listOf(remote(remoteId = 2)))

        assertEquals(listOf("Mine", "Remote 2"), noteDao.observeVisible().first().map(NoteEntity::title))
    }

    @Test
    fun `given a note, when deleted and restored, then it is hidden and shown again`() = runTest {
        noteDao.upsertAll(listOf(local(title = "Mine", updatedAt = 1)))
        val id = noteDao.observeVisible().first().single().id

        assertEquals(1, noteDao.setDeleted(id, isDeleted = true))
        assertEquals(null, noteDao.observeVisibleById(id).first())

        noteDao.setDeleted(id, isDeleted = false)
        assertEquals("Mine", noteDao.observeVisibleById(id).first()?.title)
    }

    @Test
    fun `given no such note, when deleted, then no row changes`() = runTest {
        assertEquals(0, noteDao.setDeleted(id = 42, isDeleted = true))
    }

    private fun remote(
        remoteId: Long,
        title: String = "Remote $remoteId",
    ) = NoteEntity(remoteId = remoteId, title = title, body = "Body", updatedAt = 0, syncState = SyncState.SYNCED)

    private fun local(
        title: String,
        updatedAt: Long,
    ) = NoteEntity(remoteId = null, title = title, body = "Body", updatedAt = updatedAt, syncState = SyncState.LOCAL)
}

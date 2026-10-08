package com.machinarium.notesv2.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.machinarium.notesv2.core.database.model.NoteEntity
import com.machinarium.notesv2.core.database.model.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    /** The user's newest changes first; seeded notes (updatedAt = 0) keep their API order below them. */
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC, id ASC")
    fun observeVisible(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id AND isDeleted = 0")
    fun observeVisibleById(id: Long): Flow<NoteEntity?>

    /** Soft delete, so a refresh can't bring a seeded note back and Undo can restore it. Returns rows changed. */
    @Query("UPDATE notes SET isDeleted = :isDeleted WHERE id = :id")
    suspend fun setDeleted(
        id: Long,
        isDeleted: Boolean,
    ): Int

    @Query("SELECT EXISTS(SELECT 1 FROM notes WHERE syncState = 'LOCAL' AND isDeleted = 0)")
    fun observeHasLocal(): Flow<Boolean>

    @Insert
    suspend fun insert(note: NoteEntity): Long

    /** Edits a visible note and marks it LOCAL, so a refresh never overwrites it. Returns rows changed. */
    @Query(
        "UPDATE notes SET title = :title, body = :body, updatedAt = :updatedAt, syncState = 'LOCAL' " +
            "WHERE id = :id AND isDeleted = 0",
    )
    suspend fun updateContent(
        id: Long,
        title: String,
        body: String,
        updatedAt: Long,
    ): Int

    @Query("SELECT * FROM notes WHERE remoteId IS NOT NULL")
    suspend fun getRemoteBacked(): List<NoteEntity>

    @Upsert
    suspend fun upsertAll(notes: List<NoteEntity>)

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    /**
     * Merges a fresh API copy in one transaction: new posts are inserted, unchanged ones updated, and posts that
     * are gone are removed — but rows the user edited or deleted are never touched, so a refresh can't undo them.
     */
    @Transaction
    suspend fun mergeRemote(remoteNotes: List<NoteEntity>) {
        val existing = getRemoteBacked().associateBy(NoteEntity::remoteId)
        val writable = remoteNotes.mapNotNull { remote ->
            val current = existing[remote.remoteId]
            when {
                current == null -> remote
                current.isProtectedFromRefresh() -> null
                else -> remote.copy(id = current.id)
            }
        }
        upsertAll(writable)
        // Stale ids are computed here and deleted in chunks: one NOT IN (:allRemoteIds) would exceed SQLite's
        // 999-variable limit on older devices once the API returns more than 999 notes.
        val remoteIds = remoteNotes.mapNotNullTo(HashSet(), NoteEntity::remoteId)
        existing.values
            .filter { it.syncState == SyncState.SYNCED && !it.isDeleted && it.remoteId !in remoteIds }
            .map(NoteEntity::id)
            .chunked(MAX_SQL_VARIABLES)
            .forEach { deleteByIds(it) }
    }
}

private const val MAX_SQL_VARIABLES = 900

private fun NoteEntity.isProtectedFromRefresh(): Boolean = syncState == SyncState.LOCAL || isDeleted

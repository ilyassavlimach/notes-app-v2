package com.machinarium.notesv2.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.machinarium.notesv2.core.model.Note

/**
 * One note row. Seeded notes carry the API id in [remoteId]; notes the user created have none. [syncState] and
 * [isDeleted] protect the user's changes: a refresh only rewrites rows that are still [SyncState.SYNCED].
 */
@Entity(tableName = "notes", indices = [Index(value = ["remoteId"], unique = true)])
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: Long?,
    val title: String,
    val body: String,
    /** Epoch millis of the user's last change; 0 for seeded notes, so they sort below the user's own. */
    val updatedAt: Long,
    val syncState: SyncState,
    val isDeleted: Boolean = false,
)

enum class SyncState {
    /** Identical to the API copy; a refresh may overwrite or remove it. */
    SYNCED,

    /** Created or edited by the user; a refresh never touches it. */
    LOCAL,
}

fun NoteEntity.toDomain(): Note = Note(id = id, title = title, body = body)

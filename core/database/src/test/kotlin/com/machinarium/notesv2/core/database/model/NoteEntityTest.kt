package com.machinarium.notesv2.core.database.model

import com.machinarium.notesv2.core.model.Note
import kotlin.test.assertEquals
import org.junit.Test

class NoteEntityTest {

    @Test
    fun `when mapped to domain, then id title and body are kept`() {
        val entity = NoteEntity(
            id = 9,
            remoteId = 3,
            title = "Plan",
            body = "Write tests",
            updatedAt = 42,
            syncState = SyncState.LOCAL,
        )

        assertEquals(Note(id = 9, title = "Plan", body = "Write tests"), entity.toDomain())
    }
}

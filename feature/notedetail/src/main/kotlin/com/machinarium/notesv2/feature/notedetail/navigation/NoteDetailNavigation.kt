package com.machinarium.notesv2.feature.notedetail.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.machinarium.notesv2.feature.notedetail.NoteDetailRoute
import kotlinx.serialization.Serializable

@Serializable
data class NoteDetailKey(val noteId: Long) : NavKey

/** [onBack] also closes the screen after the note was deleted. */
fun EntryProviderScope<NavKey>.noteDetailEntry(
    onBack: () -> Unit,
    onEditClick: (noteId: Long) -> Unit,
) {
    entry<NoteDetailKey> { key ->
        NoteDetailRoute(noteId = key.noteId, onBack = onBack, onEditClick = { onEditClick(key.noteId) })
    }
}

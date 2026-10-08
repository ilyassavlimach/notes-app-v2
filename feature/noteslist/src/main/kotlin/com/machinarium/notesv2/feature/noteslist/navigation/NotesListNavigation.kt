package com.machinarium.notesv2.feature.noteslist.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.machinarium.notesv2.feature.noteslist.NotesListRoute
import kotlinx.serialization.Serializable

@Serializable
data object NotesListKey : NavKey

fun EntryProviderScope<NavKey>.notesListEntry(
    onNoteClick: (noteId: Long) -> Unit,
    onAddNoteClick: () -> Unit,
) {
    entry<NotesListKey> {
        NotesListRoute(onNoteClick = onNoteClick, onAddNoteClick = onAddNoteClick)
    }
}

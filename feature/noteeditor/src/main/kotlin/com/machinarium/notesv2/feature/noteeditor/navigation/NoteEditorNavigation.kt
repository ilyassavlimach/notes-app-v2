package com.machinarium.notesv2.feature.noteeditor.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.machinarium.notesv2.feature.noteeditor.NoteEditorRoute
import kotlinx.serialization.Serializable

/** [noteId] = null creates a new note. */
@Serializable
data class NoteEditorKey(val noteId: Long? = null) : NavKey

/** [onClose] runs after a save, a discard, or a close without changes. */
fun EntryProviderScope<NavKey>.noteEditorEntry(onClose: () -> Unit) {
    entry<NoteEditorKey> { key ->
        NoteEditorRoute(noteId = key.noteId, onClose = onClose)
    }
}

package com.machinarium.notesv2.ui.navigation

import android.net.Uri
import androidx.navigation3.runtime.NavKey
import com.machinarium.notesv2.feature.notedetail.navigation.NoteDetailKey
import com.machinarium.notesv2.feature.noteslist.navigation.NotesListKey

/**
 * Turns an App Link (or a notification's deep link) into a back stack (LINK-01). Supported:
 * `https://<host>/notes/<id>` → list, then that note, so back returns to the list. Anything else opens the list.
 */
internal class DeepLinkParser(private val host: String) {

    fun parse(uri: Uri?): List<NavKey> {
        val noteId = uri
            ?.takeIf { it.scheme == HTTPS && it.host == host }
            ?.pathSegments
            ?.takeIf { it.size == NOTE_PATH_SIZE && it.first() == NOTES_SEGMENT }
            ?.last()
            ?.toLongOrNull()
            ?.takeIf { it > 0 }
        return if (noteId == null) listOf(NotesListKey) else listOf(NotesListKey, NoteDetailKey(noteId))
    }

    private companion object {
        const val HTTPS = "https"
        const val NOTES_SEGMENT = "notes"
        const val NOTE_PATH_SIZE = 2
    }
}

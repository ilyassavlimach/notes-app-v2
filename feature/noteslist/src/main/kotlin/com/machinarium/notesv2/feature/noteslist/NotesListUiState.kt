package com.machinarium.notesv2.feature.noteslist

import androidx.compose.runtime.Immutable
import com.machinarium.notesv2.core.common.result.AppError
import kotlinx.collections.immutable.ImmutableList

internal sealed interface NotesListUiState {
    /** A note deleted elsewhere that this screen still has to offer Undo for. */
    val undoNoteId: Long? get() = null

    data object Loading : NotesListUiState

    data class Empty(override val undoNoteId: Long? = null) : NotesListUiState

    data class Error(val error: AppError) : NotesListUiState

    /** Cached notes are shown; [refreshError] is a failed background refresh that leaves the cache in place. */
    data class Content(
        val notes: ImmutableList<NoteItemUi>,
        val isRefreshing: Boolean,
        val refreshError: AppError?,
        override val undoNoteId: Long? = null,
        /** The user has notes of their own, so asking for notifications makes sense now (PUSH-01). */
        val canAskForNotifications: Boolean = false,
    ) : NotesListUiState
}

@Immutable
internal data class NoteItemUi(val id: Long, val title: String, val preview: String)

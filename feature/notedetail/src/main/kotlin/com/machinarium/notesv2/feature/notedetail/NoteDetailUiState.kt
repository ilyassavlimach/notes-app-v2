package com.machinarium.notesv2.feature.notedetail

import com.machinarium.notesv2.core.common.result.AppError

internal sealed interface NoteDetailUiState {
    data object Loading : NoteDetailUiState

    /** No visible note has this id. */
    data object NotFound : NoteDetailUiState

    /** Reading the note failed; distinct from [NotFound] so the user gets a retry. */
    data class Error(val error: AppError) : NoteDetailUiState

    /** The note was deleted from this screen; the Route closes it and the list offers Undo. */
    data object Deleted : NoteDetailUiState

    data class Content(
        val title: String,
        val body: String,
        val isDeleteDialogVisible: Boolean = false,
        val deleteError: AppError? = null,
    ) : NoteDetailUiState
}

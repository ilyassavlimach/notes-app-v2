package com.machinarium.notesv2.feature.noteeditor

import com.machinarium.notesv2.core.common.result.AppError

internal sealed interface NoteEditorUiState {
    /** Loading the note to edit. */
    data object Loading : NoteEditorUiState

    /** The note to edit doesn't exist (anymore). */
    data object NotFound : NoteEditorUiState

    /** Saved or discarded; the Route closes the editor. */
    data object Closed : NoteEditorUiState

    data class Editing(
        val isNewNote: Boolean,
        val title: String,
        val body: String,
        val titleError: FieldError?,
        val bodyError: FieldError?,
        val canSave: Boolean,
        val isSaving: Boolean,
        val isDiscardDialogVisible: Boolean,
        val saveError: AppError?,
    ) : NoteEditorUiState
}

internal enum class FieldError { Required, TooLong }

package com.machinarium.notesv2.feature.noteeditor

/** Sample data shared by previews and UI tests. */
internal val PreviewEditing = NoteEditorUiState.Editing(
    isNewNote = false,
    title = "Weekly groceries",
    body = "Milk, eggs, bread and coffee beans.",
    titleError = null,
    bodyError = null,
    canSave = true,
    isSaving = false,
    isDiscardDialogVisible = false,
    saveError = null,
)

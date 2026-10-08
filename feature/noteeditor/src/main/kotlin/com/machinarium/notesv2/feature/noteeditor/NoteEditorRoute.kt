package com.machinarium.notesv2.feature.noteeditor

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun NoteEditorRoute(
    noteId: Long?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteEditorViewModel = hiltViewModel<NoteEditorViewModel, NoteEditorViewModel.Factory>(
        creationCallback = { factory -> factory.create(noteId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnClose by rememberUpdatedState(onClose)

    if (uiState == NoteEditorUiState.Closed) {
        // Closing removes this entry and its ViewModel, so the Closed state can't be replayed (COMP-08).
        LaunchedEffect(Unit) { currentOnClose() }
    }
    // System back asks before throwing away typed text, like the close button.
    BackHandler(enabled = uiState is NoteEditorUiState.Editing, onBack = viewModel::onCloseClick)

    NoteEditorScreen(
        uiState = uiState,
        isNewNote = noteId == null,
        onTitleChange = viewModel::onTitleChange,
        onBodyChange = viewModel::onBodyChange,
        onSaveClick = viewModel::onSaveClick,
        onCloseClick = viewModel::onCloseClick,
        onDiscardConfirm = viewModel::onDiscardConfirm,
        onDiscardDismiss = viewModel::onDiscardDismiss,
        onSaveErrorShown = viewModel::onSaveErrorShown,
        modifier = modifier,
    )
}

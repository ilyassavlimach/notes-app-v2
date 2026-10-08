package com.machinarium.notesv2.feature.notedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun NoteDetailRoute(
    noteId: Long,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteDetailViewModel = hiltViewModel<NoteDetailViewModel, NoteDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(noteId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)

    if (uiState == NoteDetailUiState.Deleted) {
        // Leaving removes this entry and its ViewModel, so the Deleted state can't be replayed (COMP-08).
        LaunchedEffect(Unit) { currentOnBack() }
    }

    NoteDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        onEditClick = onEditClick,
        onDeleteClick = viewModel::onDeleteClick,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteDismiss = viewModel::onDeleteDismiss,
        onDeleteErrorShown = viewModel::onDeleteErrorShown,
        modifier = modifier,
    )
}

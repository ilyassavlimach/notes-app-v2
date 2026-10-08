package com.machinarium.notesv2.feature.noteslist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun NotesListRoute(
    onNoteClick: (noteId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotesListScreen(
        uiState = uiState,
        onNoteClick = onNoteClick,
        onRefresh = viewModel::onRefresh,
        onRefreshErrorShown = viewModel::onRefreshErrorShown,
        onUndoDelete = viewModel::onUndoDelete,
        onUndoOffered = viewModel::onUndoOffered,
        modifier = modifier,
    )
}

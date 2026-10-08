package com.machinarium.notesv2.feature.noteslist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.machinarium.notesv2.core.notifications.rememberNotificationPermissionState

@Composable
internal fun NotesListRoute(
    onNoteClick: (noteId: Long) -> Unit,
    onAddNoteClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notificationPermission = rememberNotificationPermissionState()

    NotesListScreen(
        uiState = uiState,
        isNotificationPermissionGranted = notificationPermission.isGranted,
        onNoteClick = onNoteClick,
        onAddNoteClick = onAddNoteClick,
        onRefresh = viewModel::onRefresh,
        onRefreshErrorShown = viewModel::onRefreshErrorShown,
        onUndoDelete = viewModel::onUndoDelete,
        onUndoOffered = viewModel::onUndoOffered,
        onAllowNotificationsClick = notificationPermission::request,
        modifier = modifier,
    )
}

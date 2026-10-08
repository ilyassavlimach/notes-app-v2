package com.machinarium.notesv2.feature.noteslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.designsystem.components.NotesV2FloatingActionButton
import com.machinarium.notesv2.core.designsystem.components.NotesV2TopAppBar
import com.machinarium.notesv2.core.designsystem.icon.NotesV2Icons
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import com.machinarium.notesv2.core.i18n.R
import com.machinarium.notesv2.core.ui.EmptyState
import com.machinarium.notesv2.core.ui.ErrorState
import com.machinarium.notesv2.core.ui.LoadingState
import com.machinarium.notesv2.core.ui.messageRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun NotesListScreen(
    uiState: NotesListUiState,
    isNotificationPermissionGranted: Boolean,
    onNoteClick: (noteId: Long) -> Unit,
    onAddNoteClick: () -> Unit,
    onRefresh: () -> Unit,
    onRefreshErrorShown: () -> Unit,
    onUndoDelete: (noteId: Long) -> Unit,
    onUndoOffered: () -> Unit,
    onAllowNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // "Not now" holds for this session (and survives rotation); the card comes back on the next app start.
    var isRationaleDismissed by rememberSaveable { mutableStateOf(false) }
    val showRationale = (uiState as? NotesListUiState.Content)?.canAskForNotifications == true &&
        !isNotificationPermissionGranted &&
        !isRationaleDismissed
    val snackbarHostState = remember { SnackbarHostState() }
    RefreshErrorEffect(uiState, snackbarHostState, onRefreshErrorShown)
    NoteDeletedEffect(uiState.undoNoteId, snackbarHostState, onUndoDelete, onUndoOffered)

    Scaffold(
        modifier = modifier,
        topBar = { NotesV2TopAppBar(title = stringResource(R.string.noteslist_title)) },
        floatingActionButton = { AddNoteButton(onClick = onAddNoteClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        NotesListBody(
            uiState = uiState,
            onNoteClick = onNoteClick,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            header = if (showRationale) {
                {
                    NotificationRationaleCard(
                        onAllowClick = {
                            isRationaleDismissed = true
                            onAllowNotificationsClick()
                        },
                        onNotNowClick = { isRationaleDismissed = true },
                    )
                }
            } else {
                null
            },
        )
    }
}

@Composable
private fun AddNoteButton(onClick: () -> Unit) {
    NotesV2FloatingActionButton(
        icon = NotesV2Icons.Add,
        contentDescription = stringResource(R.string.noteslist_add_note),
        onClick = onClick,
    )
}

@Composable
private fun NotesListBody(
    uiState: NotesListUiState,
    onNoteClick: (noteId: Long) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    when (uiState) {
        NotesListUiState.Loading -> LoadingState(modifier = modifier)
        // Empty goes through the refreshable list so "pull down to refresh" actually works.
        is NotesListUiState.Empty -> NotesList(
            notes = persistentListOf(),
            isRefreshing = false,
            onRefresh = onRefresh,
            onNoteClick = onNoteClick,
            modifier = modifier,
        )
        is NotesListUiState.Error -> ErrorState(
            message = stringResource(uiState.error.messageRes()),
            onRetry = onRefresh,
            modifier = modifier,
        )
        is NotesListUiState.Content -> NotesList(
            notes = uiState.notes,
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            onNoteClick = onNoteClick,
            modifier = modifier,
            header = header,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesList(
    notes: ImmutableList<NoteItemUi>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNoteClick: (noteId: Long) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(NotesV2Theme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(NotesV2Theme.spacing.small),
        ) {
            header?.let { content -> item(key = HEADER_KEY) { content() } }
            if (notes.isEmpty()) {
                item(key = EMPTY_KEY) {
                    EmptyState(
                        message = stringResource(R.string.noteslist_empty),
                        modifier = Modifier.fillParentMaxSize(),
                    )
                }
            }
            items(items = notes, key = { it.id }) { note ->
                NoteListItem(note = note, onClick = { onNoteClick(note.id) })
            }
        }
    }
}

@Composable
private fun RefreshErrorEffect(
    uiState: NotesListUiState,
    snackbarHostState: SnackbarHostState,
    onShown: () -> Unit,
) {
    val error = (uiState as? NotesListUiState.Content)?.refreshError ?: return
    val message = stringResource(R.string.noteslist_refresh_failed)
    LaunchedEffect(error) {
        try {
            snackbarHostState.showSnackbar(message)
        } finally {
            // Also when the screen leaves composition mid-snackbar, so the error isn't replayed on return.
            onShown()
        }
    }
}

@Composable
private fun NoteDeletedEffect(
    noteId: Long?,
    snackbarHostState: SnackbarHostState,
    onUndo: (noteId: Long) -> Unit,
    onOffered: () -> Unit,
) {
    noteId ?: return
    val message = stringResource(R.string.noteslist_note_deleted)
    val undoLabel = stringResource(R.string.noteslist_undo)
    LaunchedEffect(noteId) {
        try {
            val result = snackbarHostState.showSnackbar(
                message,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) onUndo(noteId)
        } finally {
            onOffered() // COMP-08: Undo is offered once, even if the screen leaves mid-snackbar
        }
    }
}

private const val EMPTY_KEY = "empty"
private const val HEADER_KEY = "header"

@PreviewLightDark
@Composable
private fun NotesListScreenContentPreview() {
    NotesV2Theme {
        NotesListScreen(
            isNotificationPermissionGranted = true,
            uiState = NotesListUiState.Content(PreviewNotes, isRefreshing = false, refreshError = null),
            onNoteClick = {},
            onAddNoteClick = {},
            onRefresh = {},
            onRefreshErrorShown = {},
            onUndoDelete = {},
            onUndoOffered = {},
            onAllowNotificationsClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NotesListScreenEmptyPreview() {
    NotesV2Theme {
        NotesListScreen(
            isNotificationPermissionGranted = true,
            uiState = NotesListUiState.Empty(),
            onNoteClick = {},
            onAddNoteClick = {},
            onRefresh = {},
            onRefreshErrorShown = {},
            onUndoDelete = {},
            onUndoOffered = {},
            onAllowNotificationsClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NotesListScreenErrorPreview() {
    NotesV2Theme {
        NotesListScreen(
            isNotificationPermissionGranted = true,
            uiState = NotesListUiState.Error(AppError.Network),
            onNoteClick = {},
            onAddNoteClick = {},
            onRefresh = {},
            onRefreshErrorShown = {},
            onUndoDelete = {},
            onUndoOffered = {},
            onAllowNotificationsClick = {},
        )
    }
}

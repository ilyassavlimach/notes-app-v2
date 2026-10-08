package com.machinarium.notesv2.feature.noteslist

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.machinarium.notesv2.core.i18n.R
import com.machinarium.notesv2.core.ui.SnackbarMessageEffect

/** The screen's one-off snackbars: refresh failed, Undo failed, and "Note deleted" with Undo. */
@Composable
internal fun NotesListMessages(
    uiState: NotesListUiState,
    snackbarHostState: SnackbarHostState,
    onRefreshErrorShown: () -> Unit,
    onRestoreErrorShown: () -> Unit,
    onUndoDelete: (noteId: Long) -> Unit,
    onUndoOffered: () -> Unit,
) {
    val content = uiState as? NotesListUiState.Content
    SnackbarMessageEffect(
        trigger = content?.refreshError,
        message = stringResource(R.string.noteslist_refresh_failed),
        snackbarHostState = snackbarHostState,
        onShown = onRefreshErrorShown,
    )
    SnackbarMessageEffect(
        trigger = content?.isRestoreFailed?.takeIf { it },
        message = stringResource(R.string.noteslist_undo_failed),
        snackbarHostState = snackbarHostState,
        onShown = onRestoreErrorShown,
    )
    NoteDeletedEffect(uiState.undoNoteId, snackbarHostState, onUndoDelete, onUndoOffered)
}

/**
 * A note the user just created or edited is added at the top, but a lazy list keeps its scroll anchored to the
 * item that was first before, so the new note would sit off-screen. Scroll up when the top note changes — but
 * not on the first composition (e.g. coming back from a note), so the user's scroll position is kept.
 */
@Composable
internal fun ScrollToNewTopNoteEffect(
    topNoteId: Long?,
    listState: LazyListState,
) {
    var lastTopNoteId by rememberSaveable { mutableStateOf(topNoteId) }
    LaunchedEffect(topNoteId) {
        if (topNoteId != null && topNoteId != lastTopNoteId) listState.animateScrollToItem(0)
        lastTopNoteId = topNoteId
    }
}

@Composable
internal fun NoteDeletedEffect(
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

package com.machinarium.notesv2.feature.notedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.designsystem.components.NotesV2AlertDialog
import com.machinarium.notesv2.core.designsystem.components.NotesV2IconButton
import com.machinarium.notesv2.core.designsystem.components.NotesV2TopAppBar
import com.machinarium.notesv2.core.designsystem.icon.NotesV2Icons
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import com.machinarium.notesv2.core.i18n.R
import com.machinarium.notesv2.core.ui.EmptyState
import com.machinarium.notesv2.core.ui.ErrorState
import com.machinarium.notesv2.core.ui.LoadingState
import com.machinarium.notesv2.core.ui.messageRes

@Composable
internal fun NoteDetailScreen(
    uiState: NoteDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onDeleteErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    DeleteErrorEffect((uiState as? NoteDetailUiState.Content)?.deleteError, snackbarHostState, onDeleteErrorShown)

    Scaffold(
        modifier = modifier,
        topBar = { NoteDetailTopAppBar(isContent = uiState is NoteDetailUiState.Content, onBack, onDeleteClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (uiState) {
            NoteDetailUiState.Loading, NoteDetailUiState.Deleted -> LoadingState(modifier = contentModifier)
            NoteDetailUiState.NotFound -> EmptyState(
                message = stringResource(R.string.notedetail_not_found),
                modifier = contentModifier,
            )
            is NoteDetailUiState.Error -> ErrorState(
                message = stringResource(uiState.error.messageRes()),
                onRetry = onRetry,
                modifier = contentModifier,
            )
            is NoteDetailUiState.Content -> {
                NoteContent(title = uiState.title, body = uiState.body, modifier = contentModifier)
                if (uiState.isDeleteDialogVisible) {
                    DeleteConfirmDialog(onConfirm = onDeleteConfirm, onDismiss = onDeleteDismiss)
                }
            }
        }
    }
}

@Composable
private fun NoteDetailTopAppBar(
    isContent: Boolean,
    onBack: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    NotesV2TopAppBar(
        title = stringResource(R.string.notedetail_title),
        navigationIcon = NotesV2Icons.Back,
        navigationContentDescription = stringResource(R.string.common_back),
        onNavigationClick = onBack,
        actions = {
            if (isContent) {
                NotesV2IconButton(
                    icon = NotesV2Icons.Delete,
                    contentDescription = stringResource(R.string.notedetail_delete),
                    onClick = onDeleteClick,
                )
            }
        },
    )
}

@Composable
private fun NoteContent(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(NotesV2Theme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(NotesV2Theme.spacing.medium),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Text(text = body, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    NotesV2AlertDialog(
        title = stringResource(R.string.notedetail_delete_confirm_title),
        text = stringResource(R.string.notedetail_delete_confirm_body),
        confirmLabel = stringResource(R.string.notedetail_delete),
        dismissLabel = stringResource(R.string.common_cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
private fun DeleteErrorEffect(
    error: AppError?,
    snackbarHostState: SnackbarHostState,
    onShown: () -> Unit,
) {
    error ?: return
    val message = stringResource(R.string.notedetail_delete_failed)
    LaunchedEffect(error) {
        try {
            snackbarHostState.showSnackbar(message)
        } finally {
            onShown() // COMP-08: also when the screen leaves mid-snackbar
        }
    }
}

@PreviewLightDark
@Composable
private fun NoteDetailScreenContentPreview() {
    NotesV2Theme {
        NoteDetailScreen(
            uiState = PreviewNoteDetail,
            onBack = {},
            onRetry = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {},
            onDeleteErrorShown = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NoteDetailScreenDeleteDialogPreview() {
    NotesV2Theme {
        NoteDetailScreen(
            uiState = PreviewNoteDetail.copy(isDeleteDialogVisible = true),
            onBack = {},
            onRetry = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {},
            onDeleteErrorShown = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NoteDetailScreenNotFoundPreview() {
    NotesV2Theme {
        NoteDetailScreen(
            uiState = NoteDetailUiState.NotFound,
            onBack = {},
            onRetry = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {},
            onDeleteErrorShown = {},
        )
    }
}

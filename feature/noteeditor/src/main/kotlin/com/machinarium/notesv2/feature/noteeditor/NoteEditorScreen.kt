package com.machinarium.notesv2.feature.noteeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.components.NotesV2AlertDialog
import com.machinarium.notesv2.core.designsystem.components.NotesV2TextButton
import com.machinarium.notesv2.core.designsystem.components.NotesV2TextField
import com.machinarium.notesv2.core.designsystem.components.NotesV2TopAppBar
import com.machinarium.notesv2.core.designsystem.icon.NotesV2Icons
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import com.machinarium.notesv2.core.i18n.R
import com.machinarium.notesv2.core.ui.EmptyState
import com.machinarium.notesv2.core.ui.LoadingState
import com.machinarium.notesv2.core.ui.SnackbarMessageEffect

@Composable
internal fun NoteEditorScreen(
    uiState: NoteEditorUiState,
    isNewNote: Boolean,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCloseClick: () -> Unit,
    onDiscardConfirm: () -> Unit,
    onDiscardDismiss: () -> Unit,
    onSaveErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val editing = uiState as? NoteEditorUiState.Editing
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarMessageEffect(
        trigger = editing?.saveError,
        message = stringResource(R.string.noteeditor_save_failed),
        snackbarHostState = snackbarHostState,
        onShown = onSaveErrorShown,
    )

    Scaffold(
        modifier = modifier,
        topBar = { NoteEditorTopAppBar(isNewNote, editing, onCloseClick, onSaveClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (uiState) {
            NoteEditorUiState.Loading, NoteEditorUiState.Closed -> LoadingState(modifier = contentModifier)
            NoteEditorUiState.NotFound -> EmptyState(
                message = stringResource(R.string.common_note_not_found),
                modifier = contentModifier,
            )
            is NoteEditorUiState.Editing -> {
                NoteFields(uiState, onTitleChange, onBodyChange, modifier = contentModifier)
                if (uiState.isDiscardDialogVisible) {
                    DiscardDialog(onConfirm = onDiscardConfirm, onDismiss = onDiscardDismiss)
                }
            }
        }
    }
}

@Composable
private fun NoteEditorTopAppBar(
    isNewNote: Boolean,
    editing: NoteEditorUiState.Editing?,
    onCloseClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    // From the key, not the state: the title is right while the note loads and when it is missing.
    val titleRes = if (isNewNote) R.string.noteeditor_title_new else R.string.noteeditor_title_edit
    NotesV2TopAppBar(
        title = stringResource(titleRes),
        navigationIcon = NotesV2Icons.Close,
        navigationContentDescription = stringResource(R.string.common_close),
        onNavigationClick = onCloseClick,
        actions = {
            if (editing != null) {
                NotesV2TextButton(
                    text = stringResource(R.string.noteeditor_save),
                    onClick = onSaveClick,
                    enabled = editing.canSave,
                )
            }
        },
    )
}

@Composable
private fun NoteFields(
    state: NoteEditorUiState.Editing,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(NotesV2Theme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(NotesV2Theme.spacing.small),
    ) {
        NotesV2TextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = stringResource(R.string.noteeditor_field_title),
            supportingText = supportingText(state.titleError, state.title, NoteFieldValidator.TITLE_MAX_LENGTH),
            isError = state.titleError != null,
            singleLine = true,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        )
        NotesV2TextField(
            value = state.body,
            onValueChange = onBodyChange,
            label = stringResource(R.string.noteeditor_field_body),
            supportingText = supportingText(state.bodyError, state.body, NoteFieldValidator.BODY_MAX_LENGTH),
            isError = state.bodyError != null,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** The error when there is one, otherwise a "12/100" counter. */
@Composable
private fun supportingText(
    error: FieldError?,
    value: String,
    maxLength: Int,
): String = when (error) {
    FieldError.Required -> stringResource(R.string.noteeditor_error_title_required)
    FieldError.TooLong -> pluralStringResource(R.plurals.noteeditor_error_too_long, maxLength, maxLength)
    null -> stringResource(R.string.noteeditor_counter, value.length, maxLength)
}

@Composable
private fun DiscardDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    NotesV2AlertDialog(
        title = stringResource(R.string.noteeditor_discard_title),
        text = stringResource(R.string.noteeditor_discard_body),
        confirmLabel = stringResource(R.string.noteeditor_discard),
        dismissLabel = stringResource(R.string.noteeditor_keep_editing),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@PreviewLightDark
@Composable
private fun NoteEditorScreenEditingPreview() {
    NotesV2Theme {
        NoteEditorScreen(
            uiState = PreviewEditing,
            isNewNote = false,
            onTitleChange = {},
            onBodyChange = {},
            onSaveClick = {},
            onCloseClick = {},
            onDiscardConfirm = {},
            onDiscardDismiss = {},
            onSaveErrorShown = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NoteEditorScreenErrorPreview() {
    NotesV2Theme {
        NoteEditorScreen(
            uiState = PreviewEditing.copy(title = "", titleError = FieldError.Required, canSave = false),
            isNewNote = false,
            onTitleChange = {},
            onBodyChange = {},
            onSaveClick = {},
            onCloseClick = {},
            onDiscardConfirm = {},
            onDiscardDismiss = {},
            onSaveErrorShown = {},
        )
    }
}

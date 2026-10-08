package com.machinarium.notesv2.core.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme

/** Confirmation dialog. All texts come in as parameters — designsystem never reads string resources (DS-03). */
@Composable
fun NotesV2AlertDialog(
    title: String,
    text: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { NotesV2TextButton(text = confirmLabel, onClick = onConfirm) },
        dismissButton = { NotesV2TextButton(text = dismissLabel, onClick = onDismiss) },
        title = { Text(text = title) },
        text = { Text(text = text) },
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun NotesV2AlertDialogPreview() {
    NotesV2Theme {
        NotesV2AlertDialog(
            title = "Delete this note?",
            text = "You can undo this from the list.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

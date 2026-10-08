package com.machinarium.notesv2.feature.noteslist

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.components.NotesV2Card
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme

@Composable
internal fun NoteListItem(
    note: NoteItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NotesV2Card(onClick = onClick, modifier = modifier) {
        Text(
            text = note.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = TITLE_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = note.preview,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = PREVIEW_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val TITLE_MAX_LINES = 1
private const val PREVIEW_MAX_LINES = 2

@PreviewLightDark
@Composable
private fun NoteListItemPreview() {
    NotesV2Theme {
        NoteListItem(note = PreviewNotes.first(), onClick = {})
    }
}

package com.machinarium.notesv2.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme

/** Static card with token padding, for content with its own actions (e.g. buttons inside). */
@Composable
fun NotesV2Card(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(NotesV2Theme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(NotesV2Theme.spacing.small),
            content = content,
        )
    }
}

/** Clickable card with token padding; the whole card is one touch target. */
@Composable
fun NotesV2Card(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = NotesV2Theme.spacing.minTouchTarget),
    ) {
        Column(
            modifier = Modifier.padding(NotesV2Theme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(NotesV2Theme.spacing.extraSmall),
            content = content,
        )
    }
}

@PreviewLightDark
@Composable
private fun NotesV2CardPreview() {
    NotesV2Theme {
        NotesV2Card(onClick = {}) {
            Text(text = "Card content")
        }
    }
}

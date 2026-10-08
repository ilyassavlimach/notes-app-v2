package com.machinarium.notesv2.core.designsystem.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme

/** Low-emphasis button (dialog actions, "Not now", top-bar text actions). */
@Composable
fun NotesV2TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = NotesV2Theme.spacing.minTouchTarget),
        enabled = enabled,
    ) {
        Text(text = text)
    }
}

@PreviewLightDark
@Composable
private fun NotesV2TextButtonPreview() {
    NotesV2Theme {
        NotesV2TextButton(text = "Not now", onClick = {})
    }
}

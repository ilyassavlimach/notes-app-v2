package com.machinarium.notesv2.core.designsystem.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme

/** Primary button. Text comes in as a parameter — designsystem never reads string resources (DS-03). */
@Composable
fun NotesV2Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = NotesV2Theme.spacing.minTouchTarget),
        enabled = enabled,
    ) {
        Text(text = text)
    }
}

@PreviewLightDark
@Composable
private fun NotesV2ButtonPreview() {
    NotesV2Theme {
        NotesV2Button(text = "Retry", onClick = {})
    }
}

package com.machinarium.notesv2.core.designsystem.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.icon.NotesV2Icons
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme

/** The screen's primary action. [contentDescription] is required: the FAB has no visible label (A11Y-01). */
@Composable
fun NotesV2FloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@PreviewLightDark
@Composable
private fun NotesV2FloatingActionButtonPreview() {
    NotesV2Theme {
        NotesV2FloatingActionButton(icon = NotesV2Icons.Add, contentDescription = "Add note", onClick = {})
    }
}

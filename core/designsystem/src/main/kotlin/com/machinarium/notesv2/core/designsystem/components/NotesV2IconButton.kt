package com.machinarium.notesv2.core.designsystem.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.icon.NotesV2Icons
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme

/** Icon-only action with a 48 dp touch target. [contentDescription] is what TalkBack reads (A11Y-01). */
@Composable
fun NotesV2IconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@PreviewLightDark
@Composable
private fun NotesV2IconButtonPreview() {
    NotesV2Theme {
        NotesV2IconButton(icon = NotesV2Icons.Delete, contentDescription = "Delete", onClick = {})
    }
}

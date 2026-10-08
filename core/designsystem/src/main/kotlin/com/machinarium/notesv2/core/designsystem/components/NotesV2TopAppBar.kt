package com.machinarium.notesv2.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.icon.NotesV2Icons
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme

/**
 * Top app bar. Pass [navigationIcon] with its [navigationContentDescription] to show an up/back action, and
 * [NotesV2IconButton]s in [actions] for screen actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesV2TopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    navigationContentDescription: String? = null,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null) {
                NotesV2IconButton(
                    icon = navigationIcon,
                    contentDescription = navigationContentDescription,
                    onClick = onNavigationClick,
                )
            }
        },
        actions = actions,
    )
}

@PreviewLightDark
@Composable
private fun NotesV2TopAppBarPreview() {
    NotesV2Theme {
        NotesV2TopAppBar(
            title = "Note",
            navigationIcon = NotesV2Icons.Back,
            navigationContentDescription = "Back",
            actions = { NotesV2IconButton(icon = NotesV2Icons.Delete, contentDescription = "Delete", onClick = {}) },
        )
    }
}

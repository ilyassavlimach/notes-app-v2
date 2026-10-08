package com.machinarium.notesv2.core.config.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.config.AppGateState
import com.machinarium.notesv2.core.designsystem.components.NotesV2Button
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import com.machinarium.notesv2.core.i18n.R

/** Full-screen block shown by the app shell when the gate is not [AppGateState.Open] (RC-01). */
@Composable
fun AppGateScreen(
    state: AppGateState,
    onUpdateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state == AppGateState.Open) return
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(NotesV2Theme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(NotesV2Theme.spacing.medium, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val isUpdate = state == AppGateState.UpdateRequired
        Text(
            text = stringResource(if (isUpdate) R.string.config_update_title else R.string.config_maintenance_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(
                if (isUpdate) R.string.config_update_message else R.string.config_maintenance_message,
            ),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        if (isUpdate) {
            NotesV2Button(text = stringResource(R.string.config_update_action), onClick = onUpdateClick)
        }
    }
}

@PreviewLightDark
@Composable
private fun AppGateScreenUpdatePreview() {
    NotesV2Theme {
        AppGateScreen(state = AppGateState.UpdateRequired, onUpdateClick = {})
    }
}

@PreviewLightDark
@Composable
private fun AppGateScreenMaintenancePreview() {
    NotesV2Theme {
        AppGateScreen(state = AppGateState.Maintenance, onUpdateClick = {})
    }
}

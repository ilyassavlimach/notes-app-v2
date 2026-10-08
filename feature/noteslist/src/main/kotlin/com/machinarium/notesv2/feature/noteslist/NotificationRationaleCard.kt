package com.machinarium.notesv2.feature.noteslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.components.NotesV2Button
import com.machinarium.notesv2.core.designsystem.components.NotesV2Card
import com.machinarium.notesv2.core.designsystem.components.NotesV2TextButton
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import com.machinarium.notesv2.core.i18n.R

/** PUSH-01: explains why before the system permission dialog; "Not now" is always respected. */
@Composable
internal fun NotificationRationaleCard(
    onAllowClick: () -> Unit,
    onNotNowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NotesV2Card(modifier = modifier) {
        Text(
            text = stringResource(R.string.noteslist_notifications_rationale),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NotesV2Theme.spacing.small, Alignment.End),
        ) {
            NotesV2TextButton(text = stringResource(R.string.noteslist_notifications_not_now), onClick = onNotNowClick)
            NotesV2Button(text = stringResource(R.string.noteslist_notifications_allow), onClick = onAllowClick)
        }
    }
}

@PreviewLightDark
@Composable
private fun NotificationRationaleCardPreview() {
    NotesV2Theme {
        NotificationRationaleCard(onAllowClick = {}, onNotNowClick = {})
    }
}

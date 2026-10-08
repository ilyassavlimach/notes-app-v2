package com.machinarium.notesv2.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.components.NotesV2Button
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import com.machinarium.notesv2.core.i18n.R

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(NotesV2Theme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(NotesV2Theme.spacing.medium, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        NotesV2Button(
            text = stringResource(R.string.common_retry),
            onClick = onRetry,
        )
    }
}

@PreviewLightDark
@Composable
private fun ErrorStatePreview() {
    NotesV2Theme {
        ErrorState(message = "No internet connection.", onRetry = {})
    }
}

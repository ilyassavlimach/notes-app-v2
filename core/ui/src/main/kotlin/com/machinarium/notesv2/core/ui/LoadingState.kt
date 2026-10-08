package com.machinarium.notesv2.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import com.machinarium.notesv2.core.i18n.R

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.common_loading)
    Box(
        modifier = modifier.semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@PreviewLightDark
@Composable
private fun LoadingStatePreview() {
    NotesV2Theme {
        LoadingState()
    }
}

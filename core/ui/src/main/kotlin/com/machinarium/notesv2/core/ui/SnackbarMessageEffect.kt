package com.machinarium.notesv2.core.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Shows [message] once per [trigger] (a one-off state such as an error) and then reports [onShown], also when
 * the screen leaves composition mid-snackbar, so the message isn't replayed on return (COMP-08).
 * A null [trigger] shows nothing.
 */
@Composable
fun SnackbarMessageEffect(
    trigger: Any?,
    message: String,
    snackbarHostState: SnackbarHostState,
    onShown: () -> Unit,
) {
    trigger ?: return
    LaunchedEffect(trigger) {
        try {
            snackbarHostState.showSnackbar(message)
        } finally {
            onShown()
        }
    }
}

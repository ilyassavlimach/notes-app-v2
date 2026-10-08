package com.machinarium.notesv2.core.designsystem.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme

/**
 * Outlined text field. [supportingText] shows under the field (a counter or, with [isError], the error message,
 * which TalkBack announces with the field).
 */
@Composable
fun NotesV2TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = { Text(text = label) },
        supportingText = supportingText?.let { text -> { Text(text = text) } },
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    )
}

@PreviewLightDark
@Composable
private fun NotesV2TextFieldPreview() {
    NotesV2Theme {
        NotesV2TextField(value = "Groceries", onValueChange = {
        }, label = "Title", supportingText = "9/100", singleLine = true)
    }
}

@PreviewLightDark
@Composable
private fun NotesV2TextFieldErrorPreview() {
    NotesV2Theme {
        NotesV2TextField(value = "", onValueChange = {
        }, label = "Title", supportingText = "Add a title", isError = true)
    }
}

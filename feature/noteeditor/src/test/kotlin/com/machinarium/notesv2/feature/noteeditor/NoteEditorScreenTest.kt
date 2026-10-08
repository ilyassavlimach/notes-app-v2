package com.machinarium.notesv2.feature.noteeditor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import com.machinarium.notesv2.core.i18n.R
import com.machinarium.notesv2.core.testing.resource.stringResource
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val calls = mutableListOf<String>()

    @Test
    fun editing_showsFields_andSaveAndCloseInvokeCallbacks() {
        setScreen(PreviewEditing)

        composeRule.onNodeWithText(stringResource(R.string.noteeditor_title_edit)).assertIsDisplayed()
        composeRule.onNodeWithText(PreviewEditing.title).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.noteeditor_save)).performClick()
        composeRule.onNodeWithContentDescription(stringResource(R.string.common_close)).performClick()

        assertEquals(listOf("save", "close"), calls)
    }

    @Test
    fun typing_reportsTitleAndBodyChanges() {
        setScreen(PreviewEditing.copy(isNewNote = true, title = "", body = ""))

        composeRule.onNodeWithText(stringResource(R.string.noteeditor_title_new)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.noteeditor_field_title)).performTextInput("T")
        composeRule.onNodeWithText(stringResource(R.string.noteeditor_field_body)).performTextInput("B")

        // The screen is stateless here, so each field reports the typed text (focus changes may re-report others).
        assertEquals(true, "title:T" in calls && "body:B" in calls)
    }

    @Test
    fun titleRequired_showsError_andSaveIsDisabled() {
        setScreen(PreviewEditing.copy(title = "", titleError = FieldError.Required, canSave = false))

        composeRule.onNodeWithText(stringResource(R.string.noteeditor_error_title_required)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.noteeditor_save)).assertIsNotEnabled()
    }

    @Test
    fun discardDialog_buttonsInvokeCallbacks() {
        setScreen(PreviewEditing.copy(isDiscardDialogVisible = true))

        composeRule.onNodeWithText(stringResource(R.string.noteeditor_discard_title)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.noteeditor_keep_editing)).performClick()
        composeRule.onNodeWithText(stringResource(R.string.noteeditor_discard)).performClick()

        assertEquals(listOf("keep", "discard"), calls)
    }

    @Test
    fun saveError_showsSnackbar_andReportsShown() {
        setScreen(PreviewEditing.copy(saveError = AppError.Unknown))

        composeRule.onNodeWithText(stringResource(R.string.noteeditor_save_failed)).assertIsDisplayed()
        composeRule.mainClock.advanceTimeBy(SNACKBAR_TIMEOUT_MILLIS)
        composeRule.waitForIdle()

        assertEquals(listOf("errorShown"), calls)
    }

    @Test
    fun notFound_showsMessage() {
        setScreen(NoteEditorUiState.NotFound)

        composeRule.onNodeWithText(stringResource(R.string.common_note_not_found)).assertIsDisplayed()
    }

    @Test
    fun loading_showsProgress() {
        setScreen(NoteEditorUiState.Loading)

        composeRule.onNode(hasContentDescription(stringResource(R.string.common_loading))).assertIsDisplayed()
    }

    @Test
    fun editLoadingAndNotFound_showEditTitle() { // regression: showed "New note" while an edit loaded
        setScreen(NoteEditorUiState.Loading, isNewNote = false)

        composeRule.onNodeWithText(stringResource(R.string.noteeditor_title_edit)).assertIsDisplayed()
    }

    private fun setScreen(
        state: NoteEditorUiState,
        isNewNote: Boolean = (state as? NoteEditorUiState.Editing)?.isNewNote ?: true,
    ) {
        composeRule.setContent {
            NotesV2Theme {
                NoteEditorScreen(
                    uiState = state,
                    isNewNote = isNewNote,
                    onTitleChange = { calls += "title:$it" },
                    onBodyChange = { calls += "body:$it" },
                    onSaveClick = { calls += "save" },
                    onCloseClick = { calls += "close" },
                    onDiscardConfirm = { calls += "discard" },
                    onDiscardDismiss = { calls += "keep" },
                    onSaveErrorShown = { calls += "errorShown" },
                )
            }
        }
    }

    private companion object {
        const val SNACKBAR_TIMEOUT_MILLIS = 10_000L
    }
}

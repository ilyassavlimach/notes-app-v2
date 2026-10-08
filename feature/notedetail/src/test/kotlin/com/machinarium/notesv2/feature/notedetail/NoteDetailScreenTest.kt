package com.machinarium.notesv2.feature.notedetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class NoteDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val calls = mutableListOf<String>()

    @Test
    fun contentState_showsTitleAndBody() {
        setScreen(PreviewNoteDetail)

        composeRule.onNodeWithText(PreviewNoteDetail.title).assertIsDisplayed()
        composeRule.onNodeWithText(PreviewNoteDetail.body).assertIsDisplayed()
    }

    @Test
    fun notFoundState_showsMessage_andNoDeleteAction() {
        setScreen(NoteDetailUiState.NotFound)

        composeRule.onNodeWithText(stringResource(R.string.common_note_not_found)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(stringResource(R.string.notedetail_delete)).assertDoesNotExist()
    }

    @Test
    fun errorState_retryInvokesCallback() {
        setScreen(NoteDetailUiState.Error(AppError.Unknown))

        composeRule.onNodeWithText(stringResource(R.string.common_error_unknown)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.common_retry)).performClick()

        assertEquals(listOf("retry"), calls)
    }

    @Test
    fun loadingState_showsProgress() {
        setScreen(NoteDetailUiState.Loading)

        composeRule.onNode(hasContentDescription(stringResource(R.string.common_loading))).assertIsDisplayed()
    }

    @Test
    fun backEditAndDeleteActions_invokeCallbacks() {
        setScreen(PreviewNoteDetail)

        composeRule.onNodeWithContentDescription(stringResource(R.string.common_back)).performClick()
        composeRule.onNodeWithContentDescription(stringResource(R.string.notedetail_edit)).performClick()
        composeRule.onNodeWithContentDescription(stringResource(R.string.notedetail_delete)).performClick()

        assertEquals(listOf("back", "edit", "delete"), calls)
    }

    @Test
    fun deleteDialog_confirmAndCancel_invokeCallbacks() {
        setScreen(PreviewNoteDetail.copy(isDeleteDialogVisible = true))

        composeRule.onNodeWithText(stringResource(R.string.notedetail_delete_confirm_title)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.common_cancel)).performClick()
        composeRule.onNodeWithText(stringResource(R.string.notedetail_delete)).performClick()

        assertEquals(listOf("dismiss", "confirm"), calls)
    }

    @Test
    fun deleteError_showsSnackbar_andReportsShown() {
        setScreen(PreviewNoteDetail.copy(deleteError = AppError.Unknown))

        composeRule.onNodeWithText(stringResource(R.string.notedetail_delete_failed)).assertIsDisplayed()
        composeRule.mainClock.advanceTimeBy(SNACKBAR_TIMEOUT_MILLIS)
        composeRule.waitForIdle()

        assertEquals(listOf("errorShown"), calls)
    }

    private fun setScreen(state: NoteDetailUiState) {
        composeRule.setContent {
            NotesV2Theme {
                NoteDetailScreen(
                    uiState = state,
                    onBack = { calls += "back" },
                    onRetry = { calls += "retry" },
                    onEditClick = { calls += "edit" },
                    onDeleteClick = { calls += "delete" },
                    onDeleteConfirm = { calls += "confirm" },
                    onDeleteDismiss = { calls += "dismiss" },
                    onDeleteErrorShown = { calls += "errorShown" },
                )
            }
        }
    }

    private companion object {
        const val SNACKBAR_TIMEOUT_MILLIS = 10_000L
    }
}

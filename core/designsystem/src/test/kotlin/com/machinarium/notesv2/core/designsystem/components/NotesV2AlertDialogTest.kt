package com.machinarium.notesv2.core.designsystem.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotesV2AlertDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsTexts_andButtonsInvokeTheirCallbacks() {
        var confirms = 0
        var dismisses = 0
        composeRule.setContent {
            NotesV2Theme {
                NotesV2AlertDialog(
                    title = TITLE,
                    text = TEXT,
                    confirmLabel = CONFIRM,
                    dismissLabel = DISMISS,
                    onConfirm = { confirms++ },
                    onDismiss = { dismisses++ },
                )
            }
        }

        composeRule.onNodeWithText(TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(CONFIRM).performClick()
        composeRule.onNodeWithText(DISMISS).performClick()

        assertEquals(1 to 1, confirms to dismisses)
    }

    private companion object {
        const val TITLE = "Delete this note?"
        const val TEXT = "You can undo this from the list."
        const val CONFIRM = "Delete"
        const val DISMISS = "Cancel"
    }
}

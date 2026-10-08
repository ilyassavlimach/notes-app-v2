package com.machinarium.notesv2.core.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.machinarium.notesv2.core.designsystem.icon.NotesV2Icons
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotesV2InputComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun textField_showsLabelAndSupportingText_andReportsInput() {
        composeRule.setContent {
            NotesV2Theme {
                var value by remember { mutableStateOf("") }
                NotesV2TextField(value = value, onValueChange = {
                    value = it
                }, label = LABEL, supportingText = "${value.length}/$MAX_LENGTH")
            }
        }

        composeRule.onNodeWithText(LABEL).performTextInput(INPUT)

        composeRule.onNodeWithText("${INPUT.length}/$MAX_LENGTH").assertIsDisplayed()
    }

    @Test
    fun floatingActionButton_clickInvokesCallback() {
        var clicks = 0
        composeRule.setContent {
            NotesV2Theme {
                NotesV2FloatingActionButton(icon = NotesV2Icons.Add, contentDescription = ADD, onClick = { clicks++ })
            }
        }

        composeRule.onNodeWithContentDescription(ADD).performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun textButton_disabled_isNotEnabled() {
        composeRule.setContent { NotesV2Theme { NotesV2TextButton(text = SAVE, onClick = {}, enabled = false) } }

        composeRule.onNodeWithText(SAVE).assertIsNotEnabled()
    }

    @Test
    fun staticCard_showsContent() {
        composeRule.setContent { NotesV2Theme { NotesV2Card { Text(text = LABEL) } } }

        composeRule.onNodeWithText(LABEL).assertIsDisplayed()
    }

    private companion object {
        const val LABEL = "Title"
        const val INPUT = "Groceries"
        const val ADD = "Add note"
        const val SAVE = "Save"
        const val MAX_LENGTH = 100
    }
}

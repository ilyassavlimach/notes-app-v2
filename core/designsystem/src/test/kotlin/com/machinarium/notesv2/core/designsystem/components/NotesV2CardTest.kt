package com.machinarium.notesv2.core.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotesV2CardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun click_invokesCallback_andMeetsTouchTarget() {
        var clicks = 0
        composeRule.setContent {
            NotesV2Theme {
                NotesV2Card(onClick = { clicks++ }) { Text(text = CONTENT) }
            }
        }

        composeRule.onNodeWithText(CONTENT, useUnmergedTree = true).assertExists()
        composeRule.onNode(hasClickAction())
            .assertHasClickAction()
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        assertEquals(1, clicks)
    }

    private companion object {
        const val CONTENT = "Card content"
    }
}

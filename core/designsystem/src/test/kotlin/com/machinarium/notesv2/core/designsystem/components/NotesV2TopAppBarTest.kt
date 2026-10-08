package com.machinarium.notesv2.core.designsystem.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.machinarium.notesv2.core.designsystem.icon.NotesV2Icons
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotesV2TopAppBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsTitle() {
        composeRule.setContent { NotesV2Theme { NotesV2TopAppBar(title = TITLE) } }

        composeRule.onNodeWithText(TITLE).assertIsDisplayed()
    }

    @Test
    fun navigationAndActionClicks_invokeTheirCallbacks() {
        var backClicks = 0
        var actionClicks = 0
        composeRule.setContent {
            NotesV2Theme {
                NotesV2TopAppBar(
                    title = TITLE,
                    navigationIcon = NotesV2Icons.Back,
                    navigationContentDescription = BACK,
                    onNavigationClick = { backClicks++ },
                    actions = {
                        NotesV2IconButton(icon = NotesV2Icons.Delete, contentDescription = DELETE, onClick = {
                            actionClicks++
                        })
                    },
                )
            }
        }

        composeRule.onNodeWithContentDescription(BACK).performClick()
        composeRule.onNodeWithContentDescription(DELETE).assertTouchHeightIsEqualTo(48.dp).performClick()

        assertEquals(1 to 1, backClicks to actionClicks)
    }

    private companion object {
        const val TITLE = "Note"
        const val BACK = "Back"
        const val DELETE = "Delete"
    }
}

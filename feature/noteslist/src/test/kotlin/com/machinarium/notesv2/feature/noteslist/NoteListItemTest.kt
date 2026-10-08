package com.machinarium.notesv2.feature.noteslist

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
class NoteListItemTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsTitleAndPreview_andClickInvokesCallback() {
        val note = PreviewNotes.first()
        var clicks = 0
        composeRule.setContent {
            NotesV2Theme { NoteListItem(note = note, onClick = { clicks++ }) }
        }

        composeRule.onNodeWithText(note.title).assertIsDisplayed()
        composeRule.onNodeWithText(note.preview).assertIsDisplayed().performClick()

        assertEquals(1, clicks)
    }
}

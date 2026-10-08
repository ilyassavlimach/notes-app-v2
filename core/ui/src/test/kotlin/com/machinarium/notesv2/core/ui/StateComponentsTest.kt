package com.machinarium.notesv2.core.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import com.machinarium.notesv2.core.i18n.R
import com.machinarium.notesv2.core.testing.resource.stringResource
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StateComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_showsMessage() {
        composeRule.setContent { NotesV2Theme { EmptyState(message = MESSAGE) } }

        composeRule.onNodeWithText(MESSAGE).assertIsDisplayed()
    }

    @Test
    fun errorState_showsMessage_andRetryInvokesCallback() {
        var retries = 0
        composeRule.setContent { NotesV2Theme { ErrorState(message = MESSAGE, onRetry = { retries++ }) } }

        composeRule.onNodeWithText(MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.common_retry)).performClick()

        assertEquals(1, retries)
    }

    @Test
    fun loadingState_isAnnouncedWithLoadingDescription() {
        composeRule.setContent { NotesV2Theme { LoadingState() } }

        composeRule.onNode(hasContentDescription(stringResource(R.string.common_loading))).assertIsDisplayed()
    }

    private companion object {
        const val MESSAGE = "Nothing here"
    }
}

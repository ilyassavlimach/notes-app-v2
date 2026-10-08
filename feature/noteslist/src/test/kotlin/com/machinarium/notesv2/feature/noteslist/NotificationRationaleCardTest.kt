package com.machinarium.notesv2.feature.noteslist

import androidx.compose.ui.test.assertIsDisplayed
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
class NotificationRationaleCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsRationale_andButtonsInvokeTheirCallbacks() {
        val calls = mutableListOf<String>()
        composeRule.setContent {
            NotesV2Theme {
                NotificationRationaleCard(onAllowClick = { calls += "allow" }, onNotNowClick = { calls += "notNow" })
            }
        }

        composeRule.onNodeWithText(stringResource(R.string.noteslist_notifications_rationale)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.noteslist_notifications_not_now)).performClick()
        composeRule.onNodeWithText(stringResource(R.string.noteslist_notifications_allow)).performClick()

        assertEquals(listOf("notNow", "allow"), calls)
    }
}

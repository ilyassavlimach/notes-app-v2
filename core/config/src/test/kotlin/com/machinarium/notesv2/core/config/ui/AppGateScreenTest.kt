package com.machinarium.notesv2.core.config.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.machinarium.notesv2.core.config.AppGateState
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import com.machinarium.notesv2.core.i18n.R
import com.machinarium.notesv2.core.testing.resource.stringResource
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppGateScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun updateRequired_showsMessage_andUpdateClickInvokesCallback() {
        var clicks = 0
        setScreen(AppGateState.UpdateRequired) { clicks++ }

        composeRule.onNodeWithText(stringResource(R.string.config_update_title)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.config_update_action)).performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun maintenance_showsMaintenanceMessage() {
        setScreen(AppGateState.Maintenance)

        composeRule.onNodeWithText(stringResource(R.string.config_maintenance_title)).assertIsDisplayed()
    }

    private fun setScreen(
        state: AppGateState,
        onUpdateClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            NotesV2Theme { AppGateScreen(state = state, onUpdateClick = onUpdateClick) }
        }
    }
}

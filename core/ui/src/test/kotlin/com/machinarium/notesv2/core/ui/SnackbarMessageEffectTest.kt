package com.machinarium.notesv2.core.ui

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SnackbarMessageEffectTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenATrigger_showsTheMessage_andReportsShownOnce() {
        var shown = 0
        setContent(trigger = TRIGGER, onShown = { shown++ })

        composeRule.onNodeWithText(MESSAGE).assertIsDisplayed()
        composeRule.mainClock.advanceTimeBy(SNACKBAR_TIMEOUT_MILLIS)
        composeRule.waitForIdle()

        assertEquals(1, shown)
    }

    @Test
    fun givenNoTrigger_showsNothing() {
        var shown = 0
        setContent(trigger = null, onShown = { shown++ })

        composeRule.onNodeWithText(MESSAGE).assertDoesNotExist()
        assertEquals(0, shown)
    }

    private fun setContent(
        trigger: Any?,
        onShown: () -> Unit,
    ) {
        composeRule.setContent {
            NotesV2Theme {
                val hostState = remember { SnackbarHostState() }
                SnackbarHost(hostState)
                SnackbarMessageEffect(
                    trigger = trigger,
                    message = MESSAGE,
                    snackbarHostState = hostState,
                    onShown = onShown,
                )
            }
        }
    }

    private companion object {
        const val MESSAGE = "Couldn't save"
        const val TRIGGER = "error"
        const val SNACKBAR_TIMEOUT_MILLIS = 10_000L
    }
}

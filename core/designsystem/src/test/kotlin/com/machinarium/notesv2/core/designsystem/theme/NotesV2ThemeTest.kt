package com.machinarium.notesv2.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotesV2ThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lightTheme_usesBrandLightScheme() {
        assertEquals(LightColorScheme.primary, captureScheme(darkTheme = false).primary)
    }

    @Test
    fun darkTheme_usesBrandDarkScheme() {
        assertEquals(DarkColorScheme.primary, captureScheme(darkTheme = true).primary)
    }

    @Test
    fun spacingTokens_areProvided() {
        var spacing: NotesV2Spacing? = null
        composeRule.setContent { NotesV2Theme { spacing = NotesV2Theme.spacing } }

        assertEquals(NotesV2Spacing(), spacing)
    }

    private fun captureScheme(darkTheme: Boolean): ColorScheme {
        var scheme: ColorScheme? = null
        composeRule.setContent {
            NotesV2Theme(darkTheme = darkTheme) { scheme = MaterialTheme.colorScheme }
        }
        return requireNotNull(scheme) { "NotesV2Theme content was not composed" }
    }
}

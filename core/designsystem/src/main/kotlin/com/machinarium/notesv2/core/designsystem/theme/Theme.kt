package com.machinarium.notesv2.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

/**
 * App theme. Dynamic color is off by default so the brand colors from the design are used;
 * turn it on only if the spec asks for Material You.
 */
@Composable
fun NotesV2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    CompositionLocalProvider(LocalNotesV2Spacing provides NotesV2Spacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NotesV2Typography,
            content = content,
        )
    }
}

/** Access to the app's non-Material tokens, e.g. `NotesV2Theme.spacing.medium`. */
object NotesV2Theme {
    val spacing: NotesV2Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalNotesV2Spacing.current
}

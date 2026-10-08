package com.machinarium.notesv2.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing tokens (RES-02). Features use `NotesV2Theme.spacing.medium` instead of literal dp values. */
@Immutable
data class NotesV2Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val minTouchTarget: Dp = 48.dp,
)

internal val LocalNotesV2Spacing = staticCompositionLocalOf { NotesV2Spacing() }

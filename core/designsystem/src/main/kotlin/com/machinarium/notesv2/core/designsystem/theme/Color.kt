package com.machinarium.notesv2.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand tokens. Replace these with the values from the design source (Figma variables or
// scripts/extract_palette.py for PNGs) during the designsystem task — nowhere else may define colors.
private val Primary = Color(0xFF3B5BA9)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryDark = Color(0xFFB0C6FF)
private val OnPrimaryDark = Color(0xFF00296B)
private val Secondary = Color(0xFF575E71)
private val SecondaryDark = Color(0xFFBFC6DC)
private val Background = Color(0xFFFAF9FD)
private val BackgroundDark = Color(0xFF121318)
private val Error = Color(0xFFBA1A1A)
private val ErrorDark = Color(0xFFFFB4AB)

internal val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    background = Background,
    surface = Background,
    error = Error,
)

internal val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    secondary = SecondaryDark,
    background = BackgroundDark,
    surface = BackgroundDark,
    error = ErrorDark,
)

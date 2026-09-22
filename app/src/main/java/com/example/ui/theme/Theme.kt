package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun FalconPosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themePreset: ColorThemePreset = ColorThemePreset.AMBER,
    content: @Composable () -> Unit
) {
    val primaryColor = themePreset.primary
    val secondaryColor = themePreset.secondary

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color(0xFF1C1F22),
            primaryContainer = Color(0x33F5B700),
            onPrimaryContainer = primaryColor,
            secondary = secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = Color(0x33E8590C),
            onSecondaryContainer = secondaryColor,
            background = DarkBg,
            onBackground = DarkText,
            surface = DarkPanel,
            onSurface = DarkText,
            surfaceVariant = DarkPanelRaised,
            onSurfaceVariant = DarkTextDim,
            outline = DarkSteelLine,
            outlineVariant = DarkSteel,
            error = PosRed,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.Black,
            primaryContainer = Color(0x33F5B700),
            onPrimaryContainer = Color(0xFF8A6C05),
            secondary = secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = Color(0x33E8590C),
            onSecondaryContainer = Color(0xFFC94E0E),
            background = LightBg,
            onBackground = LightText,
            surface = LightPanel,
            onSurface = LightText,
            surfaceVariant = LightPanelRaised,
            onSurfaceVariant = LightTextDim,
            outline = LightSteelLine,
            outlineVariant = LightSteel,
            error = PosRed,
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

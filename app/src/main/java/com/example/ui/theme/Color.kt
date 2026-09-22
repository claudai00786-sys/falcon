package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Base Brand Canvas Colors
val DarkBg = Color(0xFF1C1F22)
val DarkPanel = Color(0xFF24282C)
val DarkPanelRaised = Color(0xFF2C3136)
val DarkSteelLine = Color(0xFF3A4045)
val DarkSteel = Color(0xFF4A5257)
val DarkText = Color(0xFFE9E7E2)
val DarkTextDim = Color(0xFF9AA0A6)

val LightBg = Color(0xFFF3F1EC)
val LightPanel = Color(0xFFFFFFFF)
val LightPanelRaised = Color(0xFFF7F5F0)
val LightSteelLine = Color(0xFFDCDFE3)
val LightSteel = Color(0xFFC7CCD1)
val LightText = Color(0xFF22262A)
val LightTextDim = Color(0xFF6B7278)

// Status Colors
val PosGreen = Color(0xFF4C9A5A)
val PosGreenLight = Color(0x284C9A5A)
val PosRed = Color(0xFFD14343)
val PosRedBright = Color(0xFFFF3B30)
val PosRedLight = Color(0x28D14343)
val PosAmber = Color(0xFFF5B700)
val PosOrange = Color(0xFFE8590C)
val PosSkyBlue = Color(0xFF38BDF8)

// Theme Presets
enum class ColorThemePreset(
    val title: String,
    val primary: Color,
    val secondary: Color
) {
    AMBER("Amber", Color(0xFFF5B700), Color(0xFFE8590C)),
    OCEAN("Ocean", Color(0xFF22D3EE), Color(0xFF2563EB)),
    EMERALD("Emerald", Color(0xFF34D399), Color(0xFF059669)),
    ROYAL("Royal", Color(0xFFC084FC), Color(0xFF7C3AED)),
    CRIMSON("Crimson", Color(0xFFF87171), Color(0xFFB91C1C)),
    SUNSET("Sunset", Color(0xFFFB7185), Color(0xFFF97316)),
    STEEL("Steel", Color(0xFFCBD5E1), Color(0xFF64748B))
}

package com.example.recipes.ui.theme

import androidx.compose.ui.graphics.Color

/** Colour tokens from spec §7.1. */
object RecipesColors {
    val backgroundTop = Color(0xFFD3E0E5)
    val backgroundMiddle = Color(0xFFE8EEEC)
    val backgroundBottom = Color(0xFFF3F5F2)

    val textPrimary = Color(0xFF24302D)
    val textSecondary = Color(0xFF44524E)
    val textMuted = Color(0xFF5E6B67)

    val primary = Color(0xFF4E6E5D)
    val onPrimary = Color(0xFFFFFFFF)
    val primaryContainer = Color(0xFFDCE6DE)
    val onPrimaryContainer = Color(0xFF2F4539)

    val infoContainer = Color(0xFFC9DBE8).copy(alpha = 0.7f)
    val onInfoContainer = Color(0xFF2C4456)

    val warningContainerRow = Color(0xFFF6E3E1)
    val warningContainerPill = Color(0xFFF2D6D3)
    val onWarningContainer = Color(0xFF6E2F2B)
    val warningText = Color(0xFF8A3F3B)

    val glassSurface = Color.White.copy(alpha = 0.64f)
    val glassBorder = Color.White.copy(alpha = 0.85f)

    /** The floating bottom bar sits over scrolling content without a blur, so it is a little more opaque. */
    val glassSurfaceStrong = Color.White.copy(alpha = 0.9f)

    val divider = Color(0xFF24302D).copy(alpha = 0.08f)
    val inputBorder = Color(0xFFD5DED9)

    /** Mountain header (§7.5). */
    val backRidge = Color(0xFFB4C7CC).copy(alpha = 0.55f)
    val frontRidge = Color(0xFF9FB5AB).copy(alpha = 0.45f)
    val fog = backgroundMiddle
}

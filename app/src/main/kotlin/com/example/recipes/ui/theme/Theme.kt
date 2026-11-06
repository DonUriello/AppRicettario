package com.example.recipes.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Shapes and spacing from spec §7.3. */
object RecipesDimensions {
    val screenHorizontalPadding = 20.dp
    val cardPadding = 16.dp
    val cardGap = 12.dp
    val cardRadius = 22.dp
    val bottomSheetRadius = 30.dp
    val inputRadius = 14.dp
    val primaryButtonHeight = 52.dp
    val bottomBarHeight = 68.dp
    val bottomBarRadius = 26.dp
    val bottomBarSideMargin = 14.dp
    val bottomBarBottomMargin = 20.dp

    /** Space to leave below scrolling content so the floating bottom bar does not cover it. */
    val bottomBarClearance = bottomBarHeight + bottomBarBottomMargin + 24.dp
}

private val RecipesColorScheme = lightColorScheme(
    primary = RecipesColors.primary,
    onPrimary = RecipesColors.onPrimary,
    primaryContainer = RecipesColors.primaryContainer,
    onPrimaryContainer = RecipesColors.onPrimaryContainer,
    secondary = RecipesColors.primary,
    onSecondary = RecipesColors.onPrimary,
    secondaryContainer = RecipesColors.primaryContainer,
    onSecondaryContainer = RecipesColors.onPrimaryContainer,
    background = RecipesColors.backgroundBottom,
    onBackground = RecipesColors.textPrimary,
    surface = RecipesColors.backgroundBottom,
    onSurface = RecipesColors.textPrimary,
    surfaceVariant = RecipesColors.primaryContainer,
    onSurfaceVariant = RecipesColors.textSecondary,
    surfaceContainerLow = RecipesColors.backgroundBottom,
    surfaceContainer = RecipesColors.backgroundBottom,
    surfaceContainerHigh = RecipesColors.backgroundBottom,
    outline = RecipesColors.inputBorder,
    outlineVariant = RecipesColors.divider,
    error = RecipesColors.warningText,
    onError = Color.White,
    errorContainer = RecipesColors.warningContainerRow,
    onErrorContainer = RecipesColors.onWarningContainer,
)

private val RecipesShapes = Shapes(
    extraSmall = RoundedCornerShape(RecipesDimensions.inputRadius),
    small = RoundedCornerShape(RecipesDimensions.inputRadius),
    medium = RoundedCornerShape(RecipesDimensions.cardRadius),
    large = RoundedCornerShape(RecipesDimensions.cardRadius),
    extraLarge = RoundedCornerShape(RecipesDimensions.bottomSheetRadius),
)

@Composable
fun RecipesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RecipesColorScheme,
        typography = RecipesTypography,
        shapes = RecipesShapes,
        content = content,
    )
}

/** The vertical gradient behind every screen. */
val BackgroundGradient = Brush.verticalGradient(
    0f to RecipesColors.backgroundTop,
    0.33f to RecipesColors.backgroundMiddle,
    1f to RecipesColors.backgroundBottom,
)

@Composable
fun GradientBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(BackgroundGradient), content = content)
}

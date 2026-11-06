package com.example.recipes.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.recipes.ui.theme.RecipesColors
import com.example.recipes.ui.theme.RecipesDimensions

/** Translucent white card with a thin white border over the gradient background (§7.4). */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    color: Color = RecipesColors.glassSurface,
    contentPadding: PaddingValues = PaddingValues(RecipesDimensions.cardPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RecipesDimensions.cardRadius),
        color = color,
        border = BorderStroke(1.dp, RecipesColors.glassBorder),
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun RowDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = RecipesColors.divider)
}

/** Card padding without the vertical part, for cards whose rows bring their own vertical padding. */
val ListCardPadding = PaddingValues(horizontal = RecipesDimensions.cardPadding, vertical = 4.dp)

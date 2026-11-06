package com.example.recipes.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.recipes.ui.theme.RecipesColors
import com.example.recipes.ui.theme.RecipesDimensions

/**
 * Every button has a visible text label; an icon may accompany it, never replace it.
 * Material 3 buttons already guarantee the 48 dp minimum touch target.
 */
enum class PillStyle(val container: Color, val content: Color) {
    Primary(RecipesColors.primary, RecipesColors.onPrimary),
    Tonal(RecipesColors.primaryContainer, RecipesColors.onPrimaryContainer),
    Warning(RecipesColors.warningContainerPill, RecipesColors.onWarningContainer),
    Glass(RecipesColors.glassSurface, RecipesColors.textPrimary),
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillStyle = PillStyle.Tonal,
    enabled: Boolean = true,
    @DrawableRes iconResource: Int? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 40.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.container,
            contentColor = style.content,
            disabledContainerColor = style.container.copy(alpha = 0.5f),
            disabledContentColor = RecipesColors.textMuted,
        ),
        elevation = null,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        ButtonContent(text = text, iconResource = iconResource)
    }
}

/** Main call to action: 52 dp tall, full width, with a soft shadow tinted with the primary colour. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes iconResource: Int? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(RecipesDimensions.primaryButtonHeight)
            .shadow(
                elevation = if (enabled) 10.dp else 0.dp,
                shape = CircleShape,
                ambientColor = RecipesColors.primary,
                spotColor = RecipesColors.primary,
            ),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = RecipesColors.primary,
            contentColor = RecipesColors.onPrimary,
            disabledContainerColor = RecipesColors.primaryContainer,
            disabledContentColor = RecipesColors.textSecondary,
        ),
    ) {
        ButtonContent(text = text, iconResource = iconResource)
    }
}

/** Low-emphasis text button, e.g. "Annulla", "Rimuovi", "Svuota". */
@Composable
fun LabelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = RecipesColors.primary,
    enabled: Boolean = true,
    @DrawableRes iconResource: Int? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.textButtonColors(contentColor = color, disabledContentColor = RecipesColors.textMuted),
    ) {
        ButtonContent(text = text, iconResource = iconResource)
    }
}

@Composable
private fun RowScope.ButtonContent(text: String, @DrawableRes iconResource: Int?) {
    if (iconResource != null) {
        Icon(painter = painterResource(iconResource), contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
    }
    Text(text = text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
}

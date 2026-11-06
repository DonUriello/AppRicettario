package com.example.recipes.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.recipes.ui.theme.RecipesColors

@Composable
fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = text, style = MaterialTheme.typography.labelMedium) },
        modifier = modifier.heightIn(min = 36.dp),
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = RecipesColors.glassSurface,
            labelColor = RecipesColors.textSecondary,
            selectedContainerColor = RecipesColors.primary,
            selectedLabelColor = RecipesColors.onPrimary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = RecipesColors.glassBorder,
            selectedBorderColor = Color.Transparent,
        ),
    )
}

/** Non-interactive pill label, e.g. a tag on the detail screen or the "In programma" badge. */
@Composable
fun Badge(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = RecipesColors.primaryContainer,
    content: Color = RecipesColors.onPrimaryContainer,
) {
    Surface(modifier = modifier, shape = CircleShape, color = container, contentColor = content) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

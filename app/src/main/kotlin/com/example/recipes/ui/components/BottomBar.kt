package com.example.recipes.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.recipes.ui.theme.RecipesColors
import com.example.recipes.ui.theme.RecipesDimensions

data class BottomBarItem(
    val route: String,
    val label: String,
    @DrawableRes val iconResource: Int,
)

/** Floating glass bottom navigation; the active destination is a filled pill with icon and text. */
@Composable
fun FloatingBottomBar(
    items: List<BottomBarItem>,
    selectedRoute: String?,
    onItemClick: (BottomBarItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(RecipesDimensions.bottomBarRadius)
    Surface(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                start = RecipesDimensions.bottomBarSideMargin,
                end = RecipesDimensions.bottomBarSideMargin,
                bottom = RecipesDimensions.bottomBarBottomMargin,
            )
            .fillMaxWidth()
            .height(RecipesDimensions.bottomBarHeight),
        shape = shape,
        shadowElevation = 12.dp,
        color = RecipesColors.glassSurfaceStrong,
        border = BorderStroke(1.dp, RecipesColors.glassBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp).selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEach { item ->
                val isSelected = item.route == selectedRoute
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(CircleShape)
                        .then(if (isSelected) Modifier.background(RecipesColors.primary) else Modifier)
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onItemClick(item) }),
                    contentAlignment = Alignment.Center,
                ) {
                    val contentColor = if (isSelected) RecipesColors.onPrimary else RecipesColors.textSecondary
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 6.dp)) {
                        Icon(
                            painter = painterResource(item.iconResource),
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            color = contentColor,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

package com.example.recipes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.recipes.ui.theme.GradientBackground
import com.example.recipes.ui.theme.MountainHeader
import com.example.recipes.ui.theme.RecipesColors
import com.example.recipes.ui.theme.RecipesDimensions

/**
 * Gradient background with the mountain header behind a scrolling list of cards.
 * [bottomPadding] leaves room for the floating bottom bar or a fixed bottom button.
 */
@Composable
fun MountainScreen(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = RecipesDimensions.bottomBarClearance,
    bottomContent: (@Composable () -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit,
) {
    GradientBackground(modifier = modifier) {
        MountainHeader()
        LazyColumn(
            state = listState,
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            contentPadding = PaddingValues(
                start = RecipesDimensions.screenHorizontalPadding,
                end = RecipesDimensions.screenHorizontalPadding,
                top = 12.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(RecipesDimensions.cardGap),
            content = content,
        )
        if (bottomContent != null) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) { bottomContent() }
        }
    }
}

/** Large screen title with optional trailing actions. */
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.headlineLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            actions()
        }
    }
}

/** Top row of pushed screens: a leading action, a centred title and a trailing action. */
@Composable
fun PushedScreenTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    leading: @Composable () -> Unit,
    trailing: @Composable () -> Unit = {},
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) { leading() }
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { trailing() }
    }
}

@Composable
fun CardTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        style = MaterialTheme.typography.titleLarge,
        color = RecipesColors.textPrimary,
    )
}

/** "Non hai ancora scelto cosa cucinare" + a button. */
@Composable
fun EmptyStateCard(message: String, buttonText: String, onButtonClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = RecipesColors.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton(text = buttonText, onClick = onButtonClick)
        }
    }
}

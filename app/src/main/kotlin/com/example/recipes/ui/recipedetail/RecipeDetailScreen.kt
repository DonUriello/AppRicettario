package com.example.recipes.ui.recipedetail

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipes.R
import com.example.recipes.domain.InstructionSteps
import com.example.recipes.domain.QuantityFormatter
import com.example.recipes.domain.Recipe
import com.example.recipes.domain.scaleQuantity
import com.example.recipes.ui.components.Badge
import com.example.recipes.ui.components.CardTitle
import com.example.recipes.ui.components.GlassCard
import com.example.recipes.ui.components.LabelButton
import com.example.recipes.ui.components.ListCardPadding
import com.example.recipes.ui.components.MountainScreen
import com.example.recipes.ui.components.PrimaryButton
import com.example.recipes.ui.components.PushedScreenTopBar
import com.example.recipes.ui.components.RowDivider
import com.example.recipes.ui.components.ServingsStepper
import com.example.recipes.ui.theme.RecipesColors
import com.example.recipes.ui.theme.RecipesDimensions

@Composable
fun RecipeDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recipe = uiState.recipe

    MountainScreen(
        bottomPadding = RecipesDimensions.primaryButtonHeight + 48.dp,
        bottomContent = recipe?.let {
            {
                AddToPlanButton(
                    recipe = it,
                    displayedServings = uiState.displayedServings,
                    onClick = viewModel::addToPlan,
                )
            }
        },
    ) {
        item {
            PushedScreenTopBar(
                leading = {
                    LabelButton(
                        text = stringResource(R.string.action_back_to_recipes),
                        onClick = onBack,
                        iconResource = R.drawable.ic_back,
                    )
                },
                trailing = {
                    if (recipe != null) {
                        LabelButton(text = stringResource(R.string.action_edit), onClick = { onEdit(recipe.id) })
                    }
                },
            )
        }
        when {
            uiState.isLoading -> Unit
            recipe == null -> item {
                GlassCard { Text(stringResource(R.string.detail_not_found), style = MaterialTheme.typography.bodyMedium) }
            }
            else -> recipeContent(recipe, uiState.displayedServings, viewModel)
        }
    }
}

private fun LazyListScope.recipeContent(
    recipe: Recipe,
    displayedServings: Int,
    viewModel: RecipeDetailViewModel,
) {
    item {
        Text(
            text = recipe.title,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 72.dp).semantics { heading() },
        )
    }
    if (recipe.tags.isNotEmpty() || recipe.sourceUrl != null) {
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                recipe.tags.forEach { tag -> Badge(text = tag) }
                recipe.sourceUrl?.let { sourceUrl -> SourceLink(sourceUrl) }
            }
        }
    }
    item {
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CardTitle(text = stringResource(R.string.detail_servings_label), modifier = Modifier.weight(1f))
                ServingsStepper(
                    servings = displayedServings,
                    onDecrease = viewModel::decreaseServings,
                    onIncrease = viewModel::increaseServings,
                )
            }
        }
    }
    item {
        GlassCard(contentPadding = ListCardPadding) {
            CardTitle(text = stringResource(R.string.detail_ingredients), modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            if (recipe.ingredients.isEmpty()) {
                Text(
                    stringResource(R.string.detail_no_ingredients),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RecipesColors.textMuted,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            recipe.ingredients.forEachIndexed { index, ingredient ->
                if (index > 0) RowDivider()
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = ingredient.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    val scaledQuantity = scaleQuantity(ingredient.quantity, recipe.servings, displayedServings)
                    val quantityText = QuantityFormatter.format(scaledQuantity, ingredient.unit)
                    Text(
                        text = quantityText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (ingredient.quantity == null) RecipesColors.textMuted else RecipesColors.textSecondary,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
    item {
        GlassCard {
            CardTitle(text = stringResource(R.string.detail_instructions))
            Spacer(Modifier.width(8.dp))
            val steps = InstructionSteps.split(recipe.instructions)
            if (steps.isEmpty()) {
                Text(
                    stringResource(R.string.detail_no_instructions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RecipesColors.textMuted,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            steps.forEachIndexed { index, step ->
                Row(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.titleSmall,
                        color = RecipesColors.primary,
                        modifier = Modifier.widthIn(min = 28.dp),
                    )
                    Text(text = step, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun SourceLink(sourceUrl: String) {
    val uriHandler = LocalUriHandler.current
    val domain = remember(sourceUrl) { domainOf(sourceUrl) }
    LabelButton(
        text = stringResource(R.string.detail_source, domain),
        onClick = { runCatching { uriHandler.openUri(sourceUrl) } },
        iconResource = R.drawable.ic_link,
    )
}

private fun domainOf(url: String): String {
    val host = runCatching { Uri.parse(url).host }.getOrNull() ?: return url
    return host.removePrefix("www.")
}

@Composable
private fun AddToPlanButton(recipe: Recipe, displayedServings: Int, onClick: () -> Unit) {
    val (text, enabled) = when {
        recipe.isDraft -> stringResource(R.string.detail_verify_first) to false
        recipe.plannedServings == displayedServings -> stringResource(R.string.detail_already_planned) to false
        recipe.isPlanned -> stringResource(R.string.detail_update_plan) to true
        else -> stringResource(R.string.detail_add_to_plan) to true
    }
    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = RecipesDimensions.screenHorizontalPadding, vertical = 16.dp),
    ) {
        PrimaryButton(text = text, onClick = onClick, enabled = enabled, iconResource = if (enabled) R.drawable.ic_add else null)
    }
}

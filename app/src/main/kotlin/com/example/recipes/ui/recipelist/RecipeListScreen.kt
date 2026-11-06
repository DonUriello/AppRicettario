package com.example.recipes.ui.recipelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipes.R
import com.example.recipes.domain.Recipe
import com.example.recipes.ui.components.Badge
import com.example.recipes.ui.components.CardTitle
import com.example.recipes.ui.components.FilterPill
import com.example.recipes.ui.components.GlassCard
import com.example.recipes.ui.components.LabelButton
import com.example.recipes.ui.components.ListCardPadding
import com.example.recipes.ui.components.PillButton
import com.example.recipes.ui.components.PillStyle
import com.example.recipes.ui.components.MountainScreen
import com.example.recipes.ui.components.RecipesTextField
import com.example.recipes.ui.components.RowDivider
import com.example.recipes.ui.components.ScreenTitle
import com.example.recipes.ui.theme.RecipesColors

@Composable
fun RecipeListScreen(
    onRecipeClick: (String) -> Unit,
    onNewRecipeClick: () -> Unit,
    onReviewDraftClick: (String) -> Unit,
    onCreateRecipeWithLink: (String) -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tagBeingRenamed by rememberSaveable { mutableStateOf<String?>(null) }

    MountainScreen {
        item {
            ScreenTitle(text = stringResource(R.string.recipes_title)) {
                PillButton(
                    text = stringResource(R.string.recipes_new),
                    onClick = onNewRecipeClick,
                    style = PillStyle.Primary,
                    iconResource = R.drawable.ic_add,
                )
            }
        }
        item {
            RecipesTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = stringResource(R.string.recipes_search_label),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
        }
        if (uiState.tags.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterPill(
                            text = stringResource(R.string.recipes_filter_all),
                            selected = uiState.selectedTag == null,
                            onClick = { viewModel.onTagClick(null) },
                        )
                    }
                    items(uiState.tags, key = { it }) { tag ->
                        FilterPill(text = tag, selected = tag == uiState.selectedTag, onClick = { viewModel.onTagClick(tag) })
                    }
                }
            }
        }
        uiState.selectedTag?.let { selectedTag ->
            item {
                LabelButton(
                    text = stringResource(R.string.recipes_rename_tag, selectedTag),
                    onClick = { tagBeingRenamed = selectedTag },
                )
            }
        }
        item { LinkImportCard(onDraftCreated = onReviewDraftClick, onCreateManually = onCreateRecipeWithLink) }
        item { VideoImportCard() }
        if (!uiState.isLoading) {
            item {
                GlassCard(contentPadding = ListCardPadding) {
                    when {
                        !uiState.hasAnyRecipe -> EmptyListText(stringResource(R.string.recipes_empty))
                        uiState.recipes.isEmpty() -> EmptyListText(stringResource(R.string.recipes_no_results))
                        else -> uiState.recipes.forEachIndexed { index, recipe ->
                            if (index > 0) RowDivider()
                            RecipeRow(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe.id) },
                                onAddToPlan = { viewModel.addToPlan(recipe.id) },
                                onReview = { onReviewDraftClick(recipe.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    tagBeingRenamed?.let { oldName ->
        RenameTagDialog(
            oldName = oldName,
            onDismiss = { tagBeingRenamed = null },
            onConfirm = { newName ->
                viewModel.renameTag(oldName, newName)
                tagBeingRenamed = null
            },
        )
    }
}

@Composable
private fun EmptyListText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = RecipesColors.textSecondary,
        modifier = Modifier.padding(vertical = 16.dp),
    )
}

@Composable
private fun VideoImportCard() {
    GlassCard(color = RecipesColors.infoContainer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_video),
                contentDescription = null,
                tint = RecipesColors.onInfoContainer,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.recipes_video_import_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = RecipesColors.onInfoContainer,
                )
                Text(
                    text = stringResource(R.string.recipes_video_import_coming_soon),
                    style = MaterialTheme.typography.bodySmall,
                    color = RecipesColors.onInfoContainer,
                )
            }
        }
    }
}

@Composable
private fun RecipeRow(recipe: Recipe, onClick: () -> Unit, onAddToPlan: () -> Unit, onReview: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            CardTitle(text = recipe.title)
            if (recipe.isDraft) {
                Text(
                    text = if (recipe.ingredientsToReviewCount == 0) {
                        stringResource(R.string.recipes_draft_summary_no_lines)
                    } else {
                        pluralStringResource(
                            R.plurals.recipes_draft_summary,
                            recipe.ingredientsToReviewCount,
                            recipe.ingredientsToReviewCount,
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = RecipesColors.warningText,
                )
            } else {
                Text(
                    text = stringResource(
                        R.string.recipes_summary,
                        pluralStringResource(R.plurals.servings_count, recipe.servings, recipe.servings),
                        pluralStringResource(R.plurals.ingredients_count, recipe.ingredients.size, recipe.ingredients.size),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        when {
            recipe.isDraft -> PillButton(text = stringResource(R.string.recipes_review), onClick = onReview, style = PillStyle.Warning)
            recipe.isPlanned -> Badge(text = stringResource(R.string.recipes_planned_badge))
            else -> PillButton(
                text = stringResource(R.string.recipes_add_to_plan),
                onClick = onAddToPlan,
                style = PillStyle.Tonal,
                iconResource = R.drawable.ic_add,
            )
        }
    }
}

@Composable
private fun RenameTagDialog(oldName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newName by remember(oldName) { mutableStateOf(oldName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.recipes_rename_tag_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.recipes_rename_tag_hint), style = MaterialTheme.typography.bodyMedium)
                RecipesTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = stringResource(R.string.recipes_rename_tag_field),
                )
            }
        },
        confirmButton = {
            LabelButton(
                text = stringResource(R.string.recipes_rename_tag_confirm),
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank(),
            )
        },
        dismissButton = { LabelButton(text = stringResource(R.string.action_cancel), onClick = onDismiss) },
        containerColor = RecipesColors.backgroundBottom,
    )
}

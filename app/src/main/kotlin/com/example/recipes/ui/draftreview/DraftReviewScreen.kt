package com.example.recipes.ui.draftreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipes.R
import com.example.recipes.domain.InstructionSteps
import com.example.recipes.domain.QuantityFormatter
import com.example.recipes.ui.components.CardTitle
import com.example.recipes.ui.components.EditableIngredient
import com.example.recipes.ui.components.GlassCard
import com.example.recipes.ui.components.IngredientEditorFields
import com.example.recipes.ui.components.LabelButton
import com.example.recipes.ui.components.MountainScreen
import com.example.recipes.ui.components.PillButton
import com.example.recipes.ui.components.PillStyle
import com.example.recipes.ui.components.PrimaryButton
import com.example.recipes.ui.components.PushedScreenTopBar
import com.example.recipes.ui.components.RecipesTextField
import com.example.recipes.ui.theme.RecipesColors
import com.example.recipes.ui.theme.RecipesDimensions

@Composable
fun DraftReviewScreen(
    onCancel: () -> Unit,
    onSavedAsDraft: () -> Unit,
    onSavedAsVerified: (String) -> Unit,
    onEditRest: (String) -> Unit,
    viewModel: DraftReviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DraftReviewEvent.SavedAsDraft -> onSavedAsDraft()
                is DraftReviewEvent.SavedAsVerified -> onSavedAsVerified(event.recipeId)
                is DraftReviewEvent.OpenEditor -> onEditRest(event.recipeId)
            }
        }
    }

    MountainScreen(
        modifier = Modifier.imePadding(),
        bottomPadding = RecipesDimensions.primaryButtonHeight + 48.dp,
        bottomContent = {
            Box(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = RecipesDimensions.screenHorizontalPadding, vertical = 16.dp),
            ) {
                PrimaryButton(
                    text = stringResource(R.string.review_save_verified),
                    onClick = viewModel::saveAsVerified,
                    enabled = uiState.canSaveAsVerified,
                )
            }
        },
    ) {
        item {
            PushedScreenTopBar(
                title = stringResource(R.string.review_title),
                leading = { LabelButton(text = stringResource(R.string.action_cancel), onClick = onCancel) },
                trailing = {
                    PillButton(
                        text = stringResource(R.string.review_save_draft),
                        onClick = viewModel::saveDraft,
                        enabled = uiState.recipe != null,
                    )
                },
            )
        }
        val recipe = uiState.recipe
        if (uiState.isLoading) return@MountainScreen
        if (recipe == null) {
            item { GlassCard { Text(stringResource(R.string.detail_not_found), style = MaterialTheme.typography.bodyMedium) } }
            return@MountainScreen
        }

        item { ReviewBanner(rowsToCheckCount = uiState.rowsToCheckCount) }
        item {
            GlassCard {
                RecipesTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    label = stringResource(R.string.editor_field_title),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = if (uiState.title.isBlank()) stringResource(R.string.editor_title_required) else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                Text(
                    text = pluralStringResource(R.plurals.servings_count, recipe.servings, recipe.servings) + " · " +
                        stringResource(R.string.review_steps_count, InstructionSteps.split(recipe.instructions).size),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        item { CardTitle(text = stringResource(R.string.detail_ingredients), modifier = Modifier.padding(top = 4.dp)) }
        items(uiState.ingredients, key = { it.ingredient.key }) { row ->
            ReviewIngredientCard(
                row = row,
                onConfirm = { viewModel.confirm(row.ingredient.key) },
                onEdit = { viewModel.startEditing(row.ingredient.key) },
                onRemove = { viewModel.remove(row.ingredient.key) },
                onChange = viewModel::onIngredientChange,
            )
        }
        item {
            LabelButton(
                text = stringResource(R.string.review_edit_rest),
                onClick = viewModel::editRest,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ReviewBanner(rowsToCheckCount: Int) {
    val isDone = rowsToCheckCount == 0
    GlassCard(
        modifier = Modifier.padding(top = 72.dp),
        color = if (isDone) RecipesColors.primaryContainer else RecipesColors.warningContainerRow,
    ) {
        if (isDone) {
            Text(
                text = stringResource(R.string.review_banner_done),
                style = MaterialTheme.typography.titleMedium,
                color = RecipesColors.onPrimaryContainer,
            )
        } else {
            Text(
                text = pluralStringResource(R.plurals.review_banner, rowsToCheckCount, rowsToCheckCount),
                style = MaterialTheme.typography.titleLarge,
                color = RecipesColors.onWarningContainer,
            )
            Text(
                text = stringResource(R.string.review_banner_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = RecipesColors.onWarningContainer,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ReviewIngredientCard(
    row: ReviewIngredient,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onChange: (EditableIngredient) -> Unit,
) {
    val ingredient = row.ingredient
    val cardColor = if (row.needsAttention) RecipesColors.warningContainerRow else RecipesColors.glassSurface
    GlassCard(color = cardColor) {
        if (row.isEditing) {
            IngredientEditorFields(ingredient = ingredient, onChange = onChange, onRemove = onRemove)
            Spacer(Modifier.height(8.dp))
            PillButton(text = stringResource(R.string.review_confirm), onClick = onConfirm, style = PillStyle.Primary)
            return@GlassCard
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = ingredient.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Text(
                text = QuantityFormatter.format(ingredient.toIngredient().quantity, ingredient.unit),
                style = MaterialTheme.typography.bodyLarge,
                color = RecipesColors.textSecondary,
                textAlign = TextAlign.End,
            )
        }
        if (row.isUncertain) {
            ingredient.reviewNote?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (row.needsAttention) RecipesColors.warningText else RecipesColors.textMuted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (row.isConfirmed) {
                    ConfirmedLabel()
                } else {
                    PillButton(text = stringResource(R.string.review_confirm), onClick = onConfirm, style = PillStyle.Primary)
                }
                PillButton(text = stringResource(R.string.review_edit), onClick = onEdit, style = PillStyle.Glass)
                LabelButton(text = stringResource(R.string.action_remove), onClick = onRemove, color = RecipesColors.warningText)
            }
        }
    }
}

@Composable
private fun ConfirmedLabel() {
    Surface(shape = RoundedCornerShape(50), color = RecipesColors.primaryContainer, contentColor = RecipesColors.onPrimaryContainer) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(text = stringResource(R.string.review_confirmed), style = MaterialTheme.typography.labelLarge)
        }
    }
}


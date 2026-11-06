package com.example.recipes.ui.recipeeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipes.R
import com.example.recipes.ui.components.CardTitle
import com.example.recipes.ui.components.EditableIngredient
import com.example.recipes.ui.components.IngredientEditorFields
import com.example.recipes.ui.components.FilterPill
import com.example.recipes.ui.components.GlassCard
import com.example.recipes.ui.components.LabelButton
import com.example.recipes.ui.components.MountainScreen
import com.example.recipes.ui.components.PillButton
import com.example.recipes.ui.components.PillStyle
import com.example.recipes.ui.components.PrimaryButton
import com.example.recipes.ui.components.PushedScreenTopBar
import com.example.recipes.ui.components.RecipesTextField
import com.example.recipes.ui.components.RowDivider
import com.example.recipes.ui.components.ServingsStepper
import com.example.recipes.ui.theme.RecipesColors
import com.example.recipes.ui.theme.RecipesDimensions

@Composable
fun RecipeEditorScreen(
    onCancel: () -> Unit,
    onSaved: (String) -> Unit,
    onDeleted: () -> Unit,
    viewModel: RecipeEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val existingTags by viewModel.existingTags.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is RecipeEditorEvent.Saved -> onSaved(event.recipeId)
                RecipeEditorEvent.Deleted -> onDeleted()
            }
        }
    }

    MountainScreen(modifier = Modifier.imePadding(), bottomPadding = 32.dp) {
        item {
            PushedScreenTopBar(
                title = stringResource(if (uiState.isNewRecipe) R.string.editor_title_new else R.string.editor_title_edit),
                leading = { LabelButton(text = stringResource(R.string.action_cancel), onClick = onCancel) },
                trailing = {
                    PillButton(
                        text = stringResource(R.string.action_save),
                        onClick = viewModel::save,
                        enabled = uiState.canSave,
                        style = PillStyle.Primary,
                    )
                },
            )
        }
        if (uiState.isLoading) return@MountainScreen

        item { DetailsCard(uiState, existingTags, viewModel) }
        item { IngredientsCard(uiState, viewModel) }
        item {
            GlassCard {
                CardTitle(text = stringResource(R.string.editor_instructions))
                Spacer(Modifier.height(8.dp))
                RecipesTextField(
                    value = uiState.instructions,
                    onValueChange = viewModel::onInstructionsChange,
                    label = stringResource(R.string.editor_instructions),
                    placeholder = stringResource(R.string.editor_instructions_hint),
                    singleLine = false,
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }
        }
        if (!uiState.isNewRecipe) {
            item { DeleteSection(uiState.isConfirmingDelete, viewModel) }
        }
    }

    if (uiState.isPasteSheetOpen) {
        PasteListSheet(
            text = uiState.pasteText,
            onTextChange = viewModel::onPasteTextChange,
            onConfirm = viewModel::addPastedIngredients,
            onDismiss = viewModel::closePasteSheet,
        )
    }
}

@Composable
private fun DetailsCard(uiState: RecipeEditorUiState, existingTags: List<String>, viewModel: RecipeEditorViewModel) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RecipesTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = stringResource(R.string.editor_field_title),
                modifier = Modifier.fillMaxWidth(),
                supportingText = if (uiState.title.isBlank()) stringResource(R.string.editor_title_required) else null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            )
            RecipesTextField(
                value = uiState.sourceUrl,
                onValueChange = viewModel::onSourceUrlChange,
                label = stringResource(R.string.editor_field_link),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.editor_field_servings),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                ServingsStepper(
                    servings = uiState.servings,
                    onDecrease = viewModel::decreaseServings,
                    onIncrease = viewModel::increaseServings,
                )
            }
            Text(text = stringResource(R.string.editor_field_tags), style = MaterialTheme.typography.titleSmall)
            if (uiState.tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.tags.forEach { tag -> TagWithRemove(tag = tag, onRemove = { viewModel.removeTag(tag) }) }
                }
            }
            RecipesTextField(
                value = uiState.tagInput,
                onValueChange = viewModel::onTagInputChange,
                label = stringResource(R.string.editor_field_add_tag),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.submitTagInput() }),
            )
            val query = uiState.tagInput.trim().lowercase()
            val suggestions = existingTags.filter { it !in uiState.tags && (query.isEmpty() || it.contains(query)) }
            if (suggestions.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    suggestions.forEach { suggestion ->
                        FilterPill(text = suggestion, selected = false, onClick = { viewModel.addTag(suggestion) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TagWithRemove(tag: String, onRemove: () -> Unit) {
    Surface(shape = CircleShape, color = RecipesColors.primaryContainer, contentColor = RecipesColors.onPrimaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 14.dp)) {
            Text(text = tag, style = MaterialTheme.typography.labelMedium)
            LabelButton(text = stringResource(R.string.action_take_off), onClick = onRemove, color = RecipesColors.warningText)
        }
    }
}

@Composable
private fun IngredientsCard(uiState: RecipeEditorUiState, viewModel: RecipeEditorViewModel) {
    GlassCard {
        CardTitle(text = stringResource(R.string.editor_ingredients))
        if (uiState.ingredients.isEmpty()) {
            Text(
                text = stringResource(R.string.editor_no_ingredients),
                style = MaterialTheme.typography.bodyMedium,
                color = RecipesColors.textMuted,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        }
        uiState.ingredients.forEachIndexed { index, ingredient ->
            if (index > 0) RowDivider()
            IngredientEditorRow(ingredient = ingredient, viewModel = viewModel)
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PillButton(
                text = stringResource(R.string.editor_add_ingredient),
                onClick = viewModel::addEmptyIngredient,
                iconResource = R.drawable.ic_add,
            )
            PillButton(text = stringResource(R.string.editor_paste_list), onClick = viewModel::openPasteSheet)
        }
    }
}

@Composable
private fun IngredientEditorRow(ingredient: EditableIngredient, viewModel: RecipeEditorViewModel) {
    val rowColor = if (ingredient.reviewNote != null) RecipesColors.warningContainerRow else Color.Transparent
    Surface(color = rowColor, shape = RoundedCornerShape(RecipesDimensions.inputRadius)) {
        Column(modifier = Modifier.padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ingredient.reviewNote?.let { note ->
                Text(text = note, style = MaterialTheme.typography.bodySmall, color = RecipesColors.warningText)
            }
            IngredientEditorFields(
                ingredient = ingredient,
                onChange = viewModel::updateIngredient,
                onRemove = { viewModel.removeIngredient(ingredient.key) },
            )
        }
    }
}

@Composable
private fun DeleteSection(isConfirmingDelete: Boolean, viewModel: RecipeEditorViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isConfirmingDelete) {
            LabelButton(text = stringResource(R.string.action_cancel), onClick = viewModel::cancelDelete)
            Spacer(Modifier.width(8.dp))
            PillButton(text = stringResource(R.string.editor_delete_confirm), onClick = viewModel::confirmDelete, style = PillStyle.Warning)
        } else {
            LabelButton(text = stringResource(R.string.editor_delete), onClick = viewModel::requestDelete, color = RecipesColors.warningText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasteListSheet(text: String, onTextChange: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = RecipesDimensions.bottomSheetRadius, topEnd = RecipesDimensions.bottomSheetRadius),
        containerColor = RecipesColors.backgroundBottom,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RecipesDimensions.screenHorizontalPadding)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .imePadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = stringResource(R.string.editor_paste_list_title), style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(R.string.editor_paste_list_hint), style = MaterialTheme.typography.bodyMedium, color = RecipesColors.textSecondary)
            RecipesTextField(
                value = text,
                onValueChange = onTextChange,
                label = stringResource(R.string.editor_paste_list_field),
                singleLine = false,
                minLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
            PrimaryButton(text = stringResource(R.string.editor_paste_list_confirm), onClick = onConfirm, enabled = text.isNotBlank())
            LabelButton(text = stringResource(R.string.action_cancel), onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

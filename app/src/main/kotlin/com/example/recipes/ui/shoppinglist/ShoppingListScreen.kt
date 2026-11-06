package com.example.recipes.ui.shoppinglist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipes.R
import com.example.recipes.domain.Aisle
import com.example.recipes.domain.ShoppingList
import com.example.recipes.domain.ShoppingListItem
import com.example.recipes.domain.ShoppingListTextExporter
import com.example.recipes.ui.components.CardTitle
import com.example.recipes.ui.components.EmptyStateCard
import com.example.recipes.ui.components.GlassCard
import com.example.recipes.ui.components.LabelButton
import com.example.recipes.ui.components.ListCardPadding
import com.example.recipes.ui.components.MountainScreen
import com.example.recipes.ui.components.PillButton
import com.example.recipes.ui.components.PillStyle
import com.example.recipes.ui.components.RecipesTextField
import com.example.recipes.ui.components.RowDivider
import com.example.recipes.ui.components.ScreenTitle
import com.example.recipes.ui.components.labelResource
import com.example.recipes.ui.theme.RecipesColors
import com.example.recipes.ui.theme.RecipesDimensions
import kotlinx.coroutines.launch

@Composable
fun ShoppingListScreen(
    onGoToRecipes: () -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shoppingList = uiState.shoppingList
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isStaplesSheetOpen by rememberSaveable { mutableStateOf(false) }
    val copiedMessage = stringResource(R.string.shopping_copied)
    val clipboardLabel = stringResource(R.string.shopping_clipboard_label)

    Box(modifier = Modifier.fillMaxSize()) {
        MountainScreen {
            item {
                ScreenTitle(text = stringResource(R.string.shopping_title)) {
                    if (!shoppingList.isEmpty) {
                        PillButton(
                            text = stringResource(R.string.shopping_copy),
                            iconResource = R.drawable.ic_copy,
                            onClick = {
                                val text = ShoppingListTextExporter.export(shoppingList) { aisle ->
                                    context.getString(aisle.labelResource)
                                }
                                copyToClipboard(context, clipboardLabel, text)
                                coroutineScope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                            },
                        )
                        LabelButton(text = stringResource(R.string.shopping_reset), onClick = viewModel::clearChecks)
                    }
                }
            }
            if (uiState.isLoading) return@MountainScreen
            if (shoppingList.isEmpty) {
                item {
                    EmptyStateCard(
                        message = stringResource(R.string.shopping_empty),
                        buttonText = stringResource(R.string.action_go_to_recipes),
                        onButtonClick = onGoToRecipes,
                        modifier = Modifier.padding(top = 120.dp),
                    )
                }
                return@MountainScreen
            }
            item { ProgressCard(shoppingList) }
            items(shoppingList.sections, key = { it.aisle.name }) { section ->
                AisleCard(aisle = section.aisle, items = section.items, onToggle = viewModel::toggleChecked)
            }
            item { StaplesCard(staples = shoppingList.staples, onEditStaples = { isStaplesSheetOpen = true }) }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = RecipesDimensions.bottomBarClearance),
        )
    }

    if (isStaplesSheetOpen) {
        StaplesSheet(
            stapleNames = uiState.stapleNames,
            onAdd = viewModel::addStaple,
            onRemove = viewModel::removeStaple,
            onDismiss = { isStaplesSheetOpen = false },
        )
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))
}

@Composable
private fun ProgressCard(shoppingList: ShoppingList) {
    GlassCard(modifier = Modifier.padding(top = 96.dp)) {
        Text(
            text = if (shoppingList.totalCount > 0 && shoppingList.checkedCount == shoppingList.totalCount) {
                stringResource(R.string.shopping_all_taken)
            } else {
                stringResource(R.string.shopping_progress, shoppingList.checkedCount, shoppingList.totalCount)
            },
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { if (shoppingList.totalCount == 0) 0f else shoppingList.checkedCount.toFloat() / shoppingList.totalCount },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = RecipesColors.primary,
            trackColor = RecipesColors.primaryContainer,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun AisleCard(aisle: Aisle, items: List<ShoppingListItem>, onToggle: (ShoppingListItem) -> Unit) {
    GlassCard(contentPadding = ListCardPadding) {
        CardTitle(text = stringResource(aisle.labelResource), modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
        items.forEachIndexed { index, item ->
            if (index > 0) RowDivider()
            ShoppingItemRow(item = item, onToggle = { onToggle(item) })
        }
    }
}

@Composable
private fun ShoppingItemRow(item: ShoppingListItem, onToggle: () -> Unit) {
    val decoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
    val textColor = if (item.isChecked) RecipesColors.textMuted else RecipesColors.textPrimary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = item.isChecked, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = null,
            modifier = Modifier.padding(12.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = RecipesColors.primary,
                uncheckedColor = RecipesColors.textMuted,
                checkmarkColor = RecipesColors.onPrimary,
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = MaterialTheme.typography.bodyLarge, color = textColor, textDecoration = decoration)
            Text(text = item.recipeTitles.joinToString(", "), style = MaterialTheme.typography.bodySmall, textDecoration = decoration)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = item.displayQuantity,
            style = MaterialTheme.typography.bodyLarge,
            color = if (item.isChecked || item.totalQuantity == null) RecipesColors.textMuted else RecipesColors.textSecondary,
            textDecoration = decoration,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun StaplesCard(staples: List<ShoppingListItem>, onEditStaples: () -> Unit) {
    GlassCard(color = RecipesColors.infoContainer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.shopping_staples_title),
                style = MaterialTheme.typography.titleLarge,
                color = RecipesColors.onInfoContainer,
                modifier = Modifier.weight(1f),
            )
            PillButton(text = stringResource(R.string.shopping_staples_edit), onClick = onEditStaples, style = PillStyle.Glass)
        }
        if (staples.isEmpty()) {
            Text(
                text = stringResource(R.string.shopping_staples_none),
                style = MaterialTheme.typography.bodyMedium,
                color = RecipesColors.onInfoContainer,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        staples.forEach { staple ->
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = staple.name, style = MaterialTheme.typography.bodyLarge, color = RecipesColors.onInfoContainer)
                    Text(
                        text = staple.recipeTitles.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = RecipesColors.onInfoContainer,
                    )
                }
                Text(text = staple.displayQuantity, style = MaterialTheme.typography.bodyLarge, color = RecipesColors.onInfoContainer)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StaplesSheet(
    stapleNames: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newStaple by rememberSaveable { mutableStateOf("") }
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = stringResource(R.string.staples_sheet_title), style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(R.string.staples_sheet_hint), style = MaterialTheme.typography.bodyMedium, color = RecipesColors.textSecondary)
            if (stapleNames.isEmpty()) {
                Text(text = stringResource(R.string.staples_sheet_empty), style = MaterialTheme.typography.bodyMedium)
            }
            stapleNames.forEach { stapleName ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stapleName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    LabelButton(text = stringResource(R.string.action_take_off), onClick = { onRemove(stapleName) }, color = RecipesColors.warningText)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RecipesTextField(
                    value = newStaple,
                    onValueChange = { newStaple = it },
                    label = stringResource(R.string.staples_sheet_field),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                PillButton(
                    text = stringResource(R.string.action_add),
                    enabled = newStaple.isNotBlank(),
                    style = PillStyle.Primary,
                    onClick = {
                        onAdd(newStaple)
                        newStaple = ""
                    },
                )
            }
            LabelButton(text = stringResource(R.string.action_close), onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

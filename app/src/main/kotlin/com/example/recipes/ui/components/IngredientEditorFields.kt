package com.example.recipes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.recipes.R
import com.example.recipes.domain.Aisle
import com.example.recipes.domain.MeasurementUnit
import com.example.recipes.ui.theme.RecipesColors

/** Name, quantity, unit and aisle of one ingredient, plus "Rimuovi". Used by the editor and the draft review. */
@Composable
fun IngredientEditorFields(
    ingredient: EditableIngredient,
    onChange: (EditableIngredient) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RecipesTextField(
            value = ingredient.name,
            onValueChange = { onChange(ingredient.withName(it)) },
            label = stringResource(R.string.editor_ingredient_name),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            RecipesTextField(
                value = ingredient.quantityText,
                onValueChange = { onChange(ingredient.withQuantityText(it)) },
                label = stringResource(R.string.editor_ingredient_quantity),
                modifier = Modifier.width(120.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Spacer(Modifier.width(8.dp))
            OptionPicker(
                selectedLabel = stringResource(ingredient.unit.labelResource),
                options = MeasurementUnit.entries,
                optionLabel = { stringResource(it.labelResource) },
                onSelect = { onChange(ingredient.withUnit(it)) },
            )
            Spacer(Modifier.weight(1f))
            LabelButton(text = stringResource(R.string.action_remove), onClick = onRemove, color = RecipesColors.warningText)
        }
        OptionPicker(
            selectedLabel = stringResource(R.string.editor_ingredient_aisle, aisleLabel(ingredient.aisle)),
            options = Aisle.entries,
            optionLabel = { aisleLabel(it) },
            onSelect = { onChange(ingredient.withAisle(it)) },
            style = PillStyle.Glass,
        )
    }
}

/** A pill showing the current choice; tapping it opens a menu of options. */
@Composable
fun <T> OptionPicker(
    selectedLabel: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    style: PillStyle = PillStyle.Tonal,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box {
        PillButton(text = selectedLabel, onClick = { isExpanded = true }, style = style)
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            containerColor = RecipesColors.backgroundBottom,
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option), style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onSelect(option)
                        isExpanded = false
                    },
                )
            }
        }
    }
}

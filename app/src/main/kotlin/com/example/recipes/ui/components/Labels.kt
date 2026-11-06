package com.example.recipes.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.recipes.R
import com.example.recipes.domain.Aisle
import com.example.recipes.domain.MeasurementUnit

@get:StringRes
val Aisle.labelResource: Int
    get() = when (this) {
        Aisle.PRODUCE -> R.string.aisle_produce
        Aisle.MEAT_AND_FISH -> R.string.aisle_meat_and_fish
        Aisle.DAIRY_AND_EGGS -> R.string.aisle_dairy_and_eggs
        Aisle.BREAD_PASTA_AND_RICE -> R.string.aisle_bread_pasta_and_rice
        Aisle.DRY_GOODS -> R.string.aisle_dry_goods
        Aisle.FROZEN -> R.string.aisle_frozen
        Aisle.OTHER -> R.string.aisle_other
    }

@get:StringRes
val MeasurementUnit.labelResource: Int
    get() = when (this) {
        MeasurementUnit.GRAM -> R.string.unit_gram
        MeasurementUnit.KILOGRAM -> R.string.unit_kilogram
        MeasurementUnit.MILLILITER -> R.string.unit_milliliter
        MeasurementUnit.LITER -> R.string.unit_liter
        MeasurementUnit.PIECE -> R.string.unit_piece
        MeasurementUnit.CLOVE -> R.string.unit_clove
        MeasurementUnit.TABLESPOON -> R.string.unit_tablespoon
        MeasurementUnit.TEASPOON -> R.string.unit_teaspoon
        MeasurementUnit.TO_TASTE -> R.string.unit_to_taste
    }

@Composable
fun aisleLabel(aisle: Aisle): String = stringResource(aisle.labelResource)

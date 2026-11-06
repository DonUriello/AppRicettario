package com.example.recipes.domain

enum class RecipeStatus { VERIFIED, NEEDS_REVIEW }

/** Named `MeasurementUnit` rather than `Unit` to avoid clashing with `kotlin.Unit`. */
enum class MeasurementUnit { GRAM, KILOGRAM, MILLILITER, LITER, PIECE, CLOVE, TABLESPOON, TEASPOON, TO_TASTE }

/** The declaration order is the walking order in the store. */
enum class Aisle { PRODUCE, MEAT_AND_FISH, DAIRY_AND_EGGS, BREAD_PASTA_AND_RICE, DRY_GOODS, FROZEN, OTHER }

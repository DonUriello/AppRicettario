package com.example.recipes.domain

/** One aggregated row of the shopping list, already scaled to the planned servings. */
data class ShoppingListItem(
    val name: String,
    val baseUnit: MeasurementUnit,
    /** Null for [MeasurementUnit.TO_TASTE] rows. */
    val totalQuantity: Double?,
    val aisle: Aisle,
    val recipeTitles: List<String>,
    val isStaple: Boolean,
    val isChecked: Boolean,
) {
    val displayQuantity: String get() = QuantityFormatter.format(totalQuantity, baseUnit)
}

data class AisleSection(val aisle: Aisle, val items: List<ShoppingListItem>)

data class ShoppingList(
    val sections: List<AisleSection>,
    val staples: List<ShoppingListItem>,
) {
    val itemsToBuy: List<ShoppingListItem> get() = sections.flatMap { it.items }
    val checkedCount: Int get() = itemsToBuy.count { it.isChecked }
    val totalCount: Int get() = itemsToBuy.size
    val isEmpty: Boolean get() = sections.isEmpty() && staples.isEmpty()

    companion object {
        val EMPTY = ShoppingList(sections = emptyList(), staples = emptyList())

        /** Groups non-staple items by aisle, ordering aisles by their walking order in the store. */
        fun from(items: List<ShoppingListItem>): ShoppingList {
            val (staples, toBuy) = items.partition { it.isStaple }
            val sections = toBuy
                .groupBy { it.aisle }
                .toSortedMap(compareBy { it.ordinal })
                .map { (aisle, aisleItems) -> AisleSection(aisle, aisleItems.sortedBy { it.name }) }
            return ShoppingList(sections = sections, staples = staples.sortedBy { it.name })
        }
    }
}

/**
 * Builds the plain-text list copied to the clipboard: unchecked items only, grouped by aisle.
 * [aisleLabel] provides the Italian aisle names from resources.
 */
object ShoppingListTextExporter {
    fun export(shoppingList: ShoppingList, aisleLabel: (Aisle) -> String): String =
        shoppingList.sections
            .mapNotNull { section ->
                val uncheckedItems = section.items.filterNot { it.isChecked }
                if (uncheckedItems.isEmpty()) {
                    null
                } else {
                    buildString {
                        append(aisleLabel(section.aisle))
                        uncheckedItems.forEach { item ->
                            append("\n- ").append(item.name).append(", ").append(item.displayQuantity)
                        }
                    }
                }
            }
            .joinToString("\n\n")
}

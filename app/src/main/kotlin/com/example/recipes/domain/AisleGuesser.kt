package com.example.recipes.domain

/**
 * Guesses the supermarket aisle from an ingredient name with keyword substring matching.
 *
 * Keywords overlap ("pomodor" vs "passata", "pepe" vs "peperon", "zucc" vs "zucchero"), so:
 * 1. match against the first word of the name (in Italian the head noun comes first);
 * 2. if nothing matches, match against the whole name;
 * 3. within a step, the longest matching keyword wins;
 * 4. no match gives [Aisle.OTHER].
 */
object AisleGuesser {

    /** The single place where keywords live, so the map can grow. */
    val keywordsByAisle: Map<Aisle, List<String>> = mapOf(
        Aisle.PRODUCE to listOf(
            "zucchin", "zucc", "cipoll", "aglio", "pomodor", "limon", "basilic", "prezzemol",
            "rosmarin", "salvia", "timo", "carot", "patat", "insalat", "spinac", "melanzan",
            "peperon", "funghi", "mela", "banan",
        ),
        Aisle.MEAT_AND_FISH to listOf(
            "pollo", "manzo", "maiale", "salsicc", "pesce", "salmone", "gamber", "prosciutt",
            "pancett", "guancial",
        ),
        Aisle.DAIRY_AND_EGGS to listOf(
            "uov", "latte", "burro", "parmigian", "mozzarell", "ricott", "panna", "yogurt",
            "formagg", "pecorin", "grana",
        ),
        Aisle.BREAD_PASTA_AND_RICE to listOf(
            "pasta", "spaghett", "penne", "riso", "pane", "farina", "lasagn", "gnocch", "fusill",
            "ditalin", "rigaton", "tagliatell",
        ),
        Aisle.DRY_GOODS to listOf(
            "olio", "sale", "pepe", "zucchero", "passata", "ceci", "fagiol", "lenticch", "brodo",
            "aceto", "spezi", "tonno",
        ),
        Aisle.FROZEN to listOf("surgel", "piselli"),
    )

    private val keywordAislePairs: List<Pair<String, Aisle>> =
        keywordsByAisle.flatMap { (aisle, keywords) -> keywords.map { keyword -> keyword to aisle } }

    fun guess(ingredientName: String): Aisle {
        val normalizedName = ingredientName.trim().lowercase()
        if (normalizedName.isEmpty()) return Aisle.OTHER
        val firstWord = normalizedName.split(Regex("\\s+")).first()
        return longestMatch(firstWord) ?: longestMatch(normalizedName) ?: Aisle.OTHER
    }

    private fun longestMatch(text: String): Aisle? =
        keywordAislePairs
            .filter { (keyword, _) -> text.contains(keyword) }
            .maxByOrNull { (keyword, _) -> keyword.length }
            ?.second
}

package com.example.recipes.domain

object TagNames {
    /** Tags are stored lowercase and trimmed, with inner whitespace collapsed. */
    fun normalize(rawName: String): String = rawName.trim().replace(Regex("\\s+"), " ").lowercase()
}

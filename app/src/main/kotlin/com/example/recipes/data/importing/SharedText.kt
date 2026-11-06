package com.example.recipes.data.importing

/** Browsers and apps share links as plain text, often with a title around them ("Carbonara https://…"). */
object SharedText {
    private val httpLink = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)
    private val trailingPunctuation = charArrayOf('.', ',', ';', ':', '!', '?', ')', ']', '»')

    /** The first http(s) link in [text], or null when there is none. */
    fun firstLink(text: String): String? =
        httpLink.find(text)?.value?.trimEnd(*trailingPunctuation)?.takeIf { it.length > "https://".length }

    /** What to put in the import field: the link when there is one, otherwise the trimmed text itself. */
    fun linkOrText(text: String): String = firstLink(text) ?: text.trim()
}

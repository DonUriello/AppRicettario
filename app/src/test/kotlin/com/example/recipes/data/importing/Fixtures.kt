package com.example.recipes.data.importing

object Fixtures {
    fun read(name: String): String =
        checkNotNull(Fixtures::class.java.getResource("/fixtures/$name")) { "Missing fixture $name" }.readText()
}

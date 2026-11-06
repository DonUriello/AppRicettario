package com.example.recipes.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AisleGuesserTest {

    @Test fun requiredCases() {
        val expectedAisles = mapOf(
            "passata di pomodoro" to Aisle.DRY_GOODS,
            "peperoni" to Aisle.PRODUCE,
            "petto di pollo" to Aisle.MEAT_AND_FISH,
            "zucchero" to Aisle.DRY_GOODS,
            "zucca" to Aisle.PRODUCE,
            "olio extravergine" to Aisle.DRY_GOODS,
            "riso carnaroli" to Aisle.BREAD_PASTA_AND_RICE,
        )
        expectedAisles.forEach { (name, aisle) -> assertEquals(name, aisle, AisleGuesser.guess(name)) }
    }

    @Test fun fallsBackToWholeName() = assertEquals(Aisle.PRODUCE, AisleGuesser.guess("succo di limone"))

    @Test fun caseInsensitive() = assertEquals(Aisle.PRODUCE, AisleGuesser.guess("Zucchine"))

    @Test fun unknownIsOther() {
        assertEquals(Aisle.OTHER, AisleGuesser.guess("lievito di birra"))
        assertEquals(Aisle.OTHER, AisleGuesser.guess(""))
    }
}

package com.example.recipes.data.importing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SharedTextTest {

    @Test fun plainLink() =
        assertEquals("https://www.giallozafferano.it/ricette/Carbonara.html", SharedText.firstLink("https://www.giallozafferano.it/ricette/Carbonara.html"))

    @Test fun linkAfterTitleAsBrowsersShareIt() =
        assertEquals(
            "https://blog.example.it/pasta-e-patate?utm_source=share",
            SharedText.firstLink("Pasta e patate della nonna\nhttps://blog.example.it/pasta-e-patate?utm_source=share"),
        )

    @Test fun trailingPunctuationIsDropped() =
        assertEquals("https://example.com/ricetta", SharedText.firstLink("Guarda questa (https://example.com/ricetta)."))

    @Test fun firstOfSeveralLinks() =
        assertEquals("http://a.example/uno", SharedText.firstLink("http://a.example/uno e https://b.example/due"))

    @Test fun noLink() {
        assertNull(SharedText.firstLink("Ricordati di comprare il pane"))
        assertEquals("Ricordati di comprare il pane", SharedText.linkOrText("  Ricordati di comprare il pane "))
    }
}

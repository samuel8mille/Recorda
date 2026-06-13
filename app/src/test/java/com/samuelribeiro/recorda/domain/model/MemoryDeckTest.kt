package com.samuelribeiro.recorda.domain.model

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemoryDeckTest {

    @Test
    fun `isNotEmpty is false for empty deck`() {
        assertFalse(MemoryDeck(emptyList()).isNotEmpty)
    }

    @Test
    fun `isNotEmpty is true when deck has cards`() {
        val deck = MemoryDeck(listOf(MemoryCard("0", "Conceito", "Definição")))
        assertTrue(deck.isNotEmpty)
    }
}

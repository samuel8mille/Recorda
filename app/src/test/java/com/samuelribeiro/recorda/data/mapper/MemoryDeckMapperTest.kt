package com.samuelribeiro.recorda.data.mapper

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemoryDeckMapperTest {

    private val mapper = MemoryDeckMapper()

    @Test
    fun `toMemoryDeck parses well-formed lines`() {
        val raw = """
            C: Fotossíntese | D: Processo que converte luz em energia química.
            C: Mitose | D: Divisão celular que gera duas células idênticas.
        """.trimIndent()

        val deck = mapper.toMemoryDeck(raw)

        assertEquals(2, deck.cards.size)
        assertEquals("0", deck.cards[0].id)
        assertEquals("Fotossíntese", deck.cards[0].concept)
        assertEquals("Processo que converte luz em energia química.", deck.cards[0].definition)
        assertEquals("1", deck.cards[1].id)
        assertEquals("Mitose", deck.cards[1].concept)
    }

    @Test
    fun `toMemoryDeck ignores malformed lines`() {
        val raw = """
            C: Válido | D: Definição válida
            linha sem marcadores
            C: Sem definição
            D: Sem conceito | C: ordem errada
        """.trimIndent()

        val deck = mapper.toMemoryDeck(raw)

        assertEquals(1, deck.cards.size)
        assertEquals("Válido", deck.cards[0].concept)
    }

    @Test
    fun `toMemoryDeck trims leading dashes from concept`() {
        val deck = mapper.toMemoryDeck("- C: - Conceito | D: Definição")

        assertEquals(1, deck.cards.size)
        assertEquals("Conceito", deck.cards[0].concept)
    }

    @Test
    fun `toMemoryDeck returns empty deck when no valid lines`() {
        val deck = mapper.toMemoryDeck("texto qualquer\noutra linha")

        assertTrue(deck.cards.isEmpty())
        assertTrue(!deck.isNotEmpty)
    }
}

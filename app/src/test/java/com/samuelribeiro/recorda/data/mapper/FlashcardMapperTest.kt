package com.samuelribeiro.recorda.data.mapper

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlashcardMapperTest {

    private val mapper = FlashcardMapper()

    @Test
    fun `parses valid line into Flashcard`() {
        val result = mapper.toFlashcards("P: What is Kotlin? | R: A JVM language")

        assertEquals(1, result.size)
        assertEquals("What is Kotlin?", result[0].question)
        assertEquals("A JVM language", result[0].answer)
    }

    @Test
    fun `parses multiple valid lines`() {
        val raw = """
            P: Q1? | R: A1
            P: Q2? | R: A2
            P: Q3? | R: A3
        """.trimIndent()

        val result = mapper.toFlashcards(raw)

        assertEquals(3, result.size)
    }

    @Test
    fun `skips line without pipe separator`() {
        val result = mapper.toFlashcards("This line has no separator")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `skips line with blank question`() {
        val result = mapper.toFlashcards("P:  | R: Some answer")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `skips line with blank answer`() {
        val result = mapper.toFlashcards("P: Some question | R: ")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns only valid lines from mixed input`() {
        val raw = """
            P: Valid question | R: Valid answer
            invalid line without separator
            P:  | R: blank question
        """.trimIndent()

        val result = mapper.toFlashcards(raw)

        assertEquals(1, result.size)
        assertEquals("Valid question", result[0].question)
    }

    @Test
    fun `returns empty list for blank input`() {
        assertTrue(mapper.toFlashcards("").isEmpty())
        assertTrue(mapper.toFlashcards("   ").isEmpty())
    }

    @Test
    fun `trims whitespace from question and answer`() {
        val result = mapper.toFlashcards("P:   What is it?   |   R:   It is this   ")

        assertEquals("What is it?", result[0].question)
        assertEquals("It is this", result[0].answer)
    }
}

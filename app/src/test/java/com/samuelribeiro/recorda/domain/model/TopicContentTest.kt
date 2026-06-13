package com.samuelribeiro.recorda.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TopicContentTest {

    @Test
    fun `isComplete is false when empty`() {
        assertFalse(TopicContent(emptyList()).isComplete)
    }

    @Test
    fun `isComplete is false when any body is blank`() {
        val content = TopicContent(
            listOf(
                Chapter("0", "A", "resumo", "corpo"),
                Chapter("1", "B", "resumo", body = ""),
            ),
        )
        assertFalse(content.isComplete)
    }

    @Test
    fun `isComplete is true when all bodies present`() {
        val content = TopicContent(
            listOf(Chapter("0", "A", "resumo", "corpo")),
        )
        assertTrue(content.isComplete)
    }

    @Test
    fun `asPromptSummary truncates each body to the preview length`() {
        val longBody = "x".repeat(800)
        val content = TopicContent(listOf(Chapter("0", "Título", "Resumo", longBody)))

        val summary = content.asPromptSummary()

        assertTrue(summary.contains("Título"))
        assertTrue(summary.contains("Resumo"))
        assertEquals(400, summary.count { it == 'x' })
    }
}

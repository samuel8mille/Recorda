package com.samuelribeiro.recorda.data.mapper

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TopicContentMapperTest {

    private val mapper = TopicContentMapper()

    @Test
    fun `toChapters parses well-formed lines`() {
        val raw = """
            T: Introdução | S: Visão geral do tema
            T: Conceitos | S: Definições fundamentais
        """.trimIndent()

        val chapters = mapper.toChapters(raw)

        assertEquals(2, chapters.size)
        assertEquals("0", chapters[0].id)
        assertEquals("Introdução", chapters[0].title)
        assertEquals("Visão geral do tema", chapters[0].summary)
        assertEquals("", chapters[0].body)
        assertEquals("1", chapters[1].id)
        assertEquals("Conceitos", chapters[1].title)
    }

    @Test
    fun `toChapters ignores malformed lines`() {
        val raw = """
            T: Válido | S: Resumo válido
            linha sem marcadores
            T: Sem resumo
            S: Sem título | T: ordem errada
        """.trimIndent()

        val chapters = mapper.toChapters(raw)

        assertEquals(1, chapters.size)
        assertEquals("Válido", chapters[0].title)
    }

    @Test
    fun `toChapters trims leading dashes from title`() {
        val chapters = mapper.toChapters("- T: - Título | S: Resumo")

        assertEquals(1, chapters.size)
        assertEquals("Título", chapters[0].title)
    }

    @Test
    fun `toChapters returns empty list when no valid lines`() {
        assertTrue(mapper.toChapters("texto qualquer\noutra linha").isEmpty())
    }

    @Test
    fun `toChapterBody trims and removes markdown fences`() {
        val raw = """
            ```
            Primeiro parágrafo do capítulo.

            Segundo parágrafo do capítulo.
            ```
        """.trimIndent()

        val body = mapper.toChapterBody(raw)

        assertTrue(body.startsWith("Primeiro parágrafo"))
        assertTrue(body.endsWith("Segundo parágrafo do capítulo."))
        assertTrue(!body.contains("```"))
    }
}

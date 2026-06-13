package com.samuelribeiro.recorda.data.prompt

import org.junit.Test
import kotlin.test.assertTrue

class GeminiMemoryDeckPromptBuilderTest {

    private val builder = GeminiMemoryDeckPromptBuilder()

    @Test
    fun `build includes topic name content summary and expected format`() {
        val prompt = builder.build("Biologia", "Capítulo Célula: a unidade da vida")

        assertTrue(prompt.contains("Biologia"))
        assertTrue(prompt.contains("Capítulo Célula: a unidade da vida"))
        assertTrue(prompt.contains("C: <conceito> | D: <definição>"))
    }
}

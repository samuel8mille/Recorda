package com.samuelribeiro.recorda.data.prompt

import org.junit.Test
import kotlin.test.assertTrue

class GeminiFlashcardPromptBuilderTest {

    private val builder = GeminiFlashcardPromptBuilder()

    @Test
    fun `build includes topic name content summary and expected format`() {
        val prompt = builder.build("Kotlin", "Resumo dos capítulos sobre Kotlin")

        assertTrue(prompt.contains("Kotlin"))
        assertTrue(prompt.contains("Resumo dos capítulos sobre Kotlin"))
        assertTrue(prompt.contains("P: <pergunta> | R: <resposta>"))
    }
}

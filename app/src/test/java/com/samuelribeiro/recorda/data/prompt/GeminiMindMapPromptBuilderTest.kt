package com.samuelribeiro.recorda.data.prompt

import org.junit.Test
import kotlin.test.assertTrue

class GeminiMindMapPromptBuilderTest {

    private val builder = GeminiMindMapPromptBuilder()

    @Test
    fun `build includes topic name content summary and outline rules`() {
        val prompt = builder.build("Kotlin", "Capítulo Intro: visão geral")

        assertTrue(prompt.contains("Kotlin"))
        assertTrue(prompt.contains("Capítulo Intro: visão geral"))
        assertTrue(prompt.contains("- "))
        assertTrue(prompt.contains("2 espaços"))
    }
}

package com.samuelribeiro.recorda.data.prompt

import org.junit.Test
import kotlin.test.assertTrue

class GeminiOralAnswerPromptBuilderTest {

    private val builder = GeminiOralAnswerPromptBuilder()

    @Test
    fun `build includes question expected and spoken answers and verdict format`() {
        val prompt = builder.build(
            question = "O que é Kotlin?",
            expectedAnswer = "Uma linguagem JVM moderna",
            spokenAnswer = "É uma linguagem para a JVM",
        )

        assertTrue(prompt.contains("O que é Kotlin?"))
        assertTrue(prompt.contains("Uma linguagem JVM moderna"))
        assertTrue(prompt.contains("É uma linguagem para a JVM"))
        assertTrue(prompt.contains("VEREDITO: <CORRECT|PARTIAL|INCORRECT> | FEEDBACK:"))
    }
}

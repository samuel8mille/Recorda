package com.samuelribeiro.recorda.data.prompt

import org.junit.Test
import kotlin.test.assertContains

class GeminiStudyGuidePromptBuilderTest {

    private val builder = GeminiStudyGuidePromptBuilder()

    @Test
    fun `prompt contains topic name`() {
        val prompt = builder.build("Revolução Francesa")

        assertContains(prompt, "Revolução Francesa")
    }

    @Test
    fun `prompt pins json-only response and schema`() {
        val prompt = builder.build("Kotlin")

        assertContains(prompt, "APENAS com JSON válido")
        assertContains(prompt, "\"sections\"")
        assertContains(prompt, "\"keyPoints\"")
    }

    @Test
    fun `prompt bounds the number of sections`() {
        val prompt = builder.build("Kotlin")

        assertContains(prompt, "3 a 5 sections")
    }
}

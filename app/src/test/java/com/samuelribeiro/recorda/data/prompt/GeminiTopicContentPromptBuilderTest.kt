package com.samuelribeiro.recorda.data.prompt

import com.samuelribeiro.recorda.domain.model.Chapter
import org.junit.Test
import kotlin.test.assertTrue

class GeminiTopicContentPromptBuilderTest {

    private val builder = GeminiTopicContentPromptBuilder()

    @Test
    fun `buildChapterList includes topic name and expected format`() {
        val prompt = builder.buildChapterList("Segunda Guerra Mundial")

        assertTrue(prompt.contains("Segunda Guerra Mundial"))
        assertTrue(prompt.contains("T: <título do capítulo> | S: <resumo do capítulo em até 2 frases>"))
    }

    @Test
    fun `buildChapterBody includes topic name title and summary`() {
        val chapter = Chapter("0", "Causas", "As origens do conflito")

        val prompt = builder.buildChapterBody("Segunda Guerra Mundial", chapter)

        assertTrue(prompt.contains("Segunda Guerra Mundial"))
        assertTrue(prompt.contains("Causas"))
        assertTrue(prompt.contains("As origens do conflito"))
    }
}

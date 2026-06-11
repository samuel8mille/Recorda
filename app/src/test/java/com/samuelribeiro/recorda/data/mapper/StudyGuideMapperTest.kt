package com.samuelribeiro.recorda.data.mapper

import com.google.gson.Gson
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StudyGuideMapperTest {

    private val mapper = StudyGuideMapper(Gson())

    private val validJson = """
        {"sections":[
          {"title":"Sintaxe","emoji":"📝","summary":"Resumo da sintaxe.","keyPoints":["Ponto 1","Ponto 2"],
           "analogy":"Como uma receita.","example":"val x = 1","mnemonic":"S de simples"},
          {"title":"Coroutines","emoji":"🧵","summary":"Concorrência leve.","keyPoints":["Suspend"]}
        ]}
    """.trimIndent()

    @Test
    fun `parses valid json into guide with index-based ids`() {
        val guide = mapper.toStudyGuide(validJson)

        assertEquals(2, guide.sections.size)
        assertEquals("0", guide.sections[0].id)
        assertEquals("1", guide.sections[1].id)
        assertEquals("Sintaxe", guide.sections[0].title)
        assertEquals("📝", guide.sections[0].emoji)
        assertEquals(listOf("Ponto 1", "Ponto 2"), guide.sections[0].keyPoints)
        assertEquals("Como uma receita.", guide.sections[0].analogy)
        assertEquals("val x = 1", guide.sections[0].example)
        assertEquals("S de simples", guide.sections[0].mnemonic)
    }

    @Test
    fun `optional fields absent become null`() {
        val guide = mapper.toStudyGuide(validJson)

        val second = guide.sections[1]
        assertNull(second.analogy)
        assertNull(second.example)
        assertNull(second.mnemonic)
        assertNull(second.imageUrl)
    }

    @Test
    fun `strips markdown fences and surrounding text`() {
        val fenced = "Claro! Aqui está:\n```json\n$validJson\n```\nEspero ter ajudado."

        val guide = mapper.toStudyGuide(fenced)

        assertEquals(2, guide.sections.size)
    }

    @Test
    fun `throws on text without json object`() {
        assertFailsWith<StudyGuideParseException> { mapper.toStudyGuide("sem json aqui") }
    }

    @Test
    fun `throws on malformed json`() {
        assertFailsWith<StudyGuideParseException> { mapper.toStudyGuide("{\"sections\": [ {") }
    }

    @Test
    fun `throws when sections are missing or empty`() {
        assertFailsWith<StudyGuideParseException> { mapper.toStudyGuide("{\"sections\":[]}") }
        assertFailsWith<StudyGuideParseException> { mapper.toStudyGuide("{}") }
    }

    @Test
    fun `drops sections with blank title`() {
        val json = """{"sections":[{"title":"","summary":"x"},{"title":"Válida","summary":"ok"}]}"""

        val guide = mapper.toStudyGuide(json)

        assertEquals(1, guide.sections.size)
        assertEquals("Válida", guide.sections[0].title)
    }

    @Test
    fun `filters blank key points`() {
        val json = """{"sections":[{"title":"T","keyPoints":["a","  ","b"]}]}"""

        val guide = mapper.toStudyGuide(json)

        assertEquals(listOf("a", "b"), guide.sections[0].keyPoints)
        assertTrue(guide.sections[0].summary.isEmpty())
    }
}

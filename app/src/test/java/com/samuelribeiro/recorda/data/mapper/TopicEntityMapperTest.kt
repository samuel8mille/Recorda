package com.samuelribeiro.recorda.data.mapper

import com.google.gson.Gson
import com.samuelribeiro.recorda.data.source.local.TopicEntity
import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.MindMapNode
import com.samuelribeiro.recorda.domain.model.StudyGuide
import com.samuelribeiro.recorda.domain.model.StudySection
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContent
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TopicEntityMapperTest {

    private val gson = Gson()
    private val mapper = TopicEntityMapper(gson)

    private val flashcards = listOf(Flashcard("What is Kotlin?", "A JVM language"))
    private val topic = Topic(id = "abc", name = "Kotlin", flashcards = flashcards)

    @Test
    fun `toDomain maps entity fields correctly`() {
        val json = gson.toJson(flashcards)
        val entity = TopicEntity(id = "abc", name = "Kotlin", flashcardsJson = json)

        val result = mapper.toDomain(entity)

        assertEquals("abc", result.id)
        assertEquals("Kotlin", result.name)
        assertEquals(1, result.flashcards.size)
        assertEquals("What is Kotlin?", result.flashcards[0].question)
        assertEquals("A JVM language", result.flashcards[0].answer)
    }

    @Test
    fun `toDomain returns empty flashcards for blank json`() {
        val entity = TopicEntity(id = "1", name = "Test", flashcardsJson = "")

        val result = mapper.toDomain(entity)

        assertTrue(result.flashcards.isEmpty())
    }

    @Test
    fun `toEntity maps topic fields correctly`() {
        val entity = mapper.toEntity(topic)

        assertEquals("abc", entity.id)
        assertEquals("Kotlin", entity.name)
        assertTrue(entity.flashcardsJson.contains("What is Kotlin?"))
    }

    @Test
    fun `round-trip toDomain toEntity preserves data`() {
        val entity = mapper.toEntity(topic)
        val restored = mapper.toDomain(entity)

        assertEquals(topic, restored)
    }

    @Test
    fun `toDomain returns null content for blank json`() {
        val entity = TopicEntity(id = "1", name = "Test", flashcardsJson = "")

        val result = mapper.toDomain(entity)

        assertNull(result.content)
    }

    @Test
    fun `round-trip preserves content`() {
        val content = TopicContent(
            chapters = listOf(Chapter(id = "0", title = "Intro", summary = "Resumo", body = "Corpo completo")),
        )
        val topicWithContent = topic.copy(content = content)

        val entity = mapper.toEntity(topicWithContent)
        val restored = mapper.toDomain(entity)

        assertEquals(topicWithContent, restored)
        assertEquals(content, restored.content)
    }

    @Test
    fun `toDomain returns null mindMap for blank json`() {
        val entity = TopicEntity(id = "1", name = "Test", flashcardsJson = "")

        val result = mapper.toDomain(entity)

        assertNull(result.mindMap)
    }

    @Test
    fun `round-trip preserves mind map`() {
        val mindMap = MindMapNode(id = "0", title = "Kotlin", children = listOf(MindMapNode(id = "0-0", title = "Sintaxe")))
        val topicWithMindMap = topic.copy(mindMap = mindMap)

        val entity = mapper.toEntity(topicWithMindMap)
        val restored = mapper.toDomain(entity)

        assertEquals(topicWithMindMap, restored)
        assertEquals(mindMap, restored.mindMap)
    }

    @Test
    fun `toDomain returns null study guide for blank json`() {
        val entity = TopicEntity(id = "1", name = "Test", flashcardsJson = "")

        val result = mapper.toDomain(entity)

        assertNull(result.studyGuide)
    }

    @Test
    fun `round-trip preserves study guide`() {
        val guide = StudyGuide(
            sections = listOf(
                StudySection(
                    id = "0",
                    title = "Sintaxe",
                    emoji = "📝",
                    definition = "Definição",
                    content = "Conteúdo completo",
                    summary = "Resumo",
                    keyPoints = listOf("Ponto 1"),
                    analogy = "Como uma receita",
                    imageUrl = "https://img/s.jpg",
                ),
            ),
        )
        val topicWithGuide = topic.copy(studyGuide = guide)

        val entity = mapper.toEntity(topicWithGuide)
        val restored = mapper.toDomain(entity)

        assertEquals(topicWithGuide, restored)
        assertEquals(guide, restored.studyGuide)
    }
}

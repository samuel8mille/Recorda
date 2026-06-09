package com.samuelribeiro.recorda.data.mapper

import com.google.gson.Gson
import com.samuelribeiro.recorda.data.source.local.TopicEntity
import com.samuelribeiro.recorda.data.source.local.TopicStatus
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.Topic
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TopicEntityMapperTest {

    private val gson = Gson()
    private val mapper = TopicEntityMapper(gson)

    private val flashcards = listOf(Flashcard("What is Kotlin?", "A JVM language"))
    private val topic = Topic(id = "abc", name = "Kotlin", flashcards = flashcards)

    @Test
    fun `toDomain maps entity fields correctly`() {
        val json = gson.toJson(flashcards)
        val entity = TopicEntity(id = "abc", name = "Kotlin", flashcardsJson = json, status = TopicStatus.DONE)

        val result = mapper.toDomain(entity)

        assertEquals("abc", result.id)
        assertEquals("Kotlin", result.name)
        assertEquals(1, result.flashcards.size)
        assertEquals("What is Kotlin?", result.flashcards[0].question)
        assertEquals("A JVM language", result.flashcards[0].answer)
    }

    @Test
    fun `toDomain returns empty flashcards for blank json`() {
        val entity = TopicEntity(id = "1", name = "Test", flashcardsJson = "", status = TopicStatus.PENDING)

        val result = mapper.toDomain(entity)

        assertTrue(result.flashcards.isEmpty())
    }

    @Test
    fun `toEntity maps topic fields correctly with DONE status`() {
        val entity = mapper.toEntity(topic)

        assertEquals("abc", entity.id)
        assertEquals("Kotlin", entity.name)
        assertEquals(TopicStatus.DONE, entity.status)
        assertTrue(entity.flashcardsJson.contains("What is Kotlin?"))
    }

    @Test
    fun `toEntity uses provided status`() {
        val entity = mapper.toEntity(topic, status = TopicStatus.PENDING)

        assertEquals(TopicStatus.PENDING, entity.status)
    }

    @Test
    fun `round-trip toDomain toEntity preserves data`() {
        val entity = mapper.toEntity(topic)
        val restored = mapper.toDomain(entity)

        assertEquals(topic, restored)
    }
}

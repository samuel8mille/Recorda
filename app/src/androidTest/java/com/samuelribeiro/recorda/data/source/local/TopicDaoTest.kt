package com.samuelribeiro.recorda.data.source.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class TopicDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TopicDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.topicDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_getAll_returns_topics_in_reverse_insertion_order() = runBlocking {
        dao.insert(TopicEntity(id = "t1", name = "Kotlin", flashcardsJson = "[]"))
        dao.insert(TopicEntity(id = "t2", name = "Coroutines", flashcardsJson = "[]"))

        val result = dao.getAll().first()

        assertEquals(listOf("t2", "t1"), result.map { it.id })
    }

    @Test
    fun getById_returns_matching_entity() = runBlocking {
        val entity = TopicEntity(id = "t1", name = "Kotlin", flashcardsJson = "[]")
        dao.insert(entity)

        val result = dao.getById("t1").first()

        assertEquals(entity, result)
    }

    @Test
    fun getById_unknown_id_returns_null() = runBlocking {
        val result = dao.getById("nonexistent").first()

        assertNull(result)
    }

    @Test
    fun updateFlashcards_updates_flashcardsJson() = runBlocking {
        dao.insert(TopicEntity(id = "t1", name = "Kotlin", flashcardsJson = "[]"))
        val json = """[{"question":"O que é Kotlin?","answer":"Linguagem JVM moderna"}]"""
        dao.updateFlashcards("t1", json)

        val result = dao.getById("t1").first()

        assertEquals(json, result?.flashcardsJson)
    }

    @Test
    fun updateContent_updates_contentJson() = runBlocking {
        dao.insert(TopicEntity(id = "t1", name = "Kotlin", flashcardsJson = "[]"))
        val json = """{"chapters":[{"id":"0","title":"Intro","summary":"s","body":"corpo"}]}"""
        dao.updateContent("t1", json)

        val result = dao.getById("t1").first()

        assertEquals(json, result?.contentJson)
    }

    @Test
    fun updateMindMap_updates_mindMapJson() = runBlocking {
        dao.insert(TopicEntity(id = "t1", name = "Kotlin", flashcardsJson = "[]"))
        val json = """{"id":"0","title":"Kotlin","children":[]}"""
        dao.updateMindMap("t1", json)

        val result = dao.getById("t1").first()

        assertEquals(json, result?.mindMapJson)
    }

    @Test
    fun updateMemoryCards_updates_memoryCardsJson() = runBlocking {
        dao.insert(TopicEntity(id = "t1", name = "Kotlin", flashcardsJson = "[]"))
        val json = """{"cards":[{"id":"0","concept":"Célula","definition":"Unidade da vida"}]}"""
        dao.updateMemoryCards("t1", json)

        val result = dao.getById("t1").first()

        assertEquals(json, result?.memoryCardsJson)
    }

    @Test
    fun deleteById_removes_topic_from_database() = runBlocking {
        dao.insert(TopicEntity(id = "t1", name = "Kotlin", flashcardsJson = "[]"))
        dao.deleteById("t1")

        val result = dao.getAll().first()

        assertTrue(result.isEmpty())
    }
}

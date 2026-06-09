package com.samuelribeiro.recorda.data.source.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class FlashcardReviewDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FlashcardReviewDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.flashcardReviewDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getReviewsForTopic_returns_empty_when_none_exist() = runBlocking {
        val result = dao.getReviewsForTopic("t1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun upsert_inserts_new_review() = runBlocking {
        val entity = FlashcardReviewEntity(id = "r1", topicId = "t1", cardIndex = 0)
        dao.upsert(entity)

        val result = dao.getReviewsForTopic("t1")

        assertEquals(1, result.size)
        assertEquals(entity, result[0])
    }

    @Test
    fun upsert_replaces_existing_review_with_updated_repetitions() = runBlocking {
        dao.upsert(FlashcardReviewEntity(id = "r1", topicId = "t1", cardIndex = 0, repetitions = 0))
        dao.upsert(FlashcardReviewEntity(id = "r1", topicId = "t1", cardIndex = 0, repetitions = 3))

        val result = dao.getReviewsForTopic("t1")

        assertEquals(1, result.size)
        assertEquals(3, result[0].repetitions)
    }

    @Test
    fun deleteByTopicId_removes_all_reviews_for_topic() = runBlocking {
        dao.upsert(FlashcardReviewEntity(id = "r1", topicId = "t1", cardIndex = 0))
        dao.upsert(FlashcardReviewEntity(id = "r2", topicId = "t1", cardIndex = 1))
        dao.deleteByTopicId("t1")

        val result = dao.getReviewsForTopic("t1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteByTopicId_does_not_affect_other_topics() = runBlocking {
        dao.upsert(FlashcardReviewEntity(id = "r1", topicId = "t1", cardIndex = 0))
        dao.upsert(FlashcardReviewEntity(id = "r2", topicId = "t2", cardIndex = 0))
        dao.deleteByTopicId("t1")

        val result = dao.getReviewsForTopic("t2")

        assertEquals(1, result.size)
        assertEquals("r2", result[0].id)
    }
}

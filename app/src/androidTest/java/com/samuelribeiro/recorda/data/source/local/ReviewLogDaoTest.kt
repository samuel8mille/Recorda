package com.samuelribeiro.recorda.data.source.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewLogDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ReviewLogDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.reviewLogDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_autogenerates_ids_and_getLogs_orders_by_timestamp() = runBlocking {
        dao.insert(ReviewLogEntity(topicId = "t1", cardIndex = 0, rating = "GOOD", timestampMillis = 200L))
        dao.insert(ReviewLogEntity(topicId = "t1", cardIndex = 1, rating = "AGAIN", timestampMillis = 100L))

        val logs = dao.getLogsForTopic("t1")

        assertEquals(listOf(100L, 200L), logs.map { it.timestampMillis })
        assertTrue(logs.all { it.id > 0 })
        assertEquals(2, logs.map { it.id }.distinct().size)
    }

    @Test
    fun getLogs_filters_by_topic() = runBlocking {
        dao.insert(ReviewLogEntity(topicId = "t1", cardIndex = 0, rating = "GOOD", timestampMillis = 1L))
        dao.insert(ReviewLogEntity(topicId = "t2", cardIndex = 0, rating = "EASY", timestampMillis = 2L))

        val logs = dao.getLogsForTopic("t1")

        assertEquals(1, logs.size)
        assertEquals("t1", logs[0].topicId)
    }

    @Test
    fun deleteByTopicId_removes_only_that_topic() = runBlocking {
        dao.insert(ReviewLogEntity(topicId = "t1", cardIndex = 0, rating = "GOOD", timestampMillis = 1L))
        dao.insert(ReviewLogEntity(topicId = "t2", cardIndex = 0, rating = "GOOD", timestampMillis = 2L))

        dao.deleteByTopicId("t1")

        assertTrue(dao.getLogsForTopic("t1").isEmpty())
        assertEquals(1, dao.getLogsForTopic("t2").size)
    }
}

package com.samuelribeiro.recorda.data.source.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncCommandDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SyncCommandDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.syncCommandDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun command(id: String, createdAt: Long, status: String = "PENDING", lastAttempt: Long? = null) =
        SyncCommandEntity(
            id = id,
            commandType = "CREATE_TOPIC",
            entityId = "t1",
            payloadJson = "{}",
            createdAtMillis = createdAt,
            status = status,
            lastAttemptAtMillis = lastAttempt,
        )

    @Test
    fun getPendingBatch_returns_only_pending_oldest_first_limited() = runBlocking {
        dao.insert(command("a", createdAt = 30))
        dao.insert(command("b", createdAt = 10))
        dao.insert(command("c", createdAt = 20, status = "SENT"))

        val batch = dao.getPendingBatch(limit = 10)

        assertEquals(listOf("b", "a"), batch.map { it.id })
    }

    @Test
    fun markSent_moves_commands_out_of_pending() = runBlocking {
        dao.insert(command("a", createdAt = 10))
        dao.insert(command("b", createdAt = 20))

        dao.markSent(listOf("a"), attemptAtMillis = 999)

        assertEquals(listOf("b"), dao.getPendingBatch(10).map { it.id })
    }

    @Test
    fun markFailed_bumps_retry_count_and_keeps_pending_when_status_pending() = runBlocking {
        dao.insert(command("a", createdAt = 10))

        dao.markFailed(listOf("a"), status = "PENDING", error = "boom", attemptAtMillis = 5)

        val pending = dao.getPendingBatch(10)
        assertEquals(1, pending.size)
        assertEquals(1, pending[0].retryCount)
        assertEquals("boom", pending[0].lastErrorMessage)
    }

    @Test
    fun markFailed_with_FAILED_status_removes_from_pending() = runBlocking {
        dao.insert(command("a", createdAt = 10))

        dao.markFailed(listOf("a"), status = "FAILED", error = "dead", attemptAtMillis = 5)

        assertEquals(emptyList<String>(), dao.getPendingBatch(10).map { it.id })
    }

    @Test
    fun pruneSent_deletes_old_sent_only() = runBlocking {
        dao.insert(command("old", createdAt = 1, status = "SENT", lastAttempt = 100))
        dao.insert(command("recent", createdAt = 2, status = "SENT", lastAttempt = 5000))
        dao.insert(command("pending", createdAt = 3))

        dao.pruneSent(olderThanMillis = 1000)

        assertEquals(listOf("pending"), dao.getPendingBatch(10).map { it.id })
        dao.markFailed(listOf("recent"), status = "PENDING", error = null, attemptAtMillis = 0)
        assertEquals(setOf("pending", "recent"), dao.getPendingBatch(10).map { it.id }.toSet())
    }
}

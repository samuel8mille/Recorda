package com.samuelribeiro.recorda.data.sync

import com.samuelribeiro.recorda.data.source.local.SyncCommandDao
import com.samuelribeiro.recorda.data.source.local.SyncCommandEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class RoomSyncQueueTest {

    private val dao: SyncCommandDao = mockk(relaxed = true)
    private val queue = RoomSyncQueue(dao)

    @Test
    fun `pending maps entities to domain commands`() = runTest {
        coEvery { dao.getPendingBatch(50) } returns listOf(
            SyncCommandEntity("id1", "CREATE_TOPIC", "t1", "{}", 100L),
        )

        val result = queue.pending(50)

        assertEquals(1, result.size)
        assertEquals("id1", result[0].id)
        assertEquals("CREATE_TOPIC", result[0].commandType)
        assertEquals("t1", result[0].entityId)
        assertEquals("{}", result[0].payloadJson)
        assertEquals(100L, result[0].createdAtMillis)
    }

    @Test
    fun `markSent delegates to dao`() = runTest {
        queue.markSent(listOf("a", "b"), 999L)

        coVerify { dao.markSent(listOf("a", "b"), 999L) }
    }

    @Test
    fun `markFailed delegates to dao`() = runTest {
        queue.markFailed(listOf("a"), "FAILED", "boom", 5L)

        coVerify { dao.markFailed(listOf("a"), "FAILED", "boom", 5L) }
    }

    @Test
    fun `pruneSent delegates to dao`() = runTest {
        queue.pruneSent(123L)

        coVerify { dao.pruneSent(123L) }
    }
}

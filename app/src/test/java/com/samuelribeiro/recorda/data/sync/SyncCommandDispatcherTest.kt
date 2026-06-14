package com.samuelribeiro.recorda.data.sync

import com.google.gson.Gson
import com.samuelribeiro.recorda.core.sync.SyncScheduler
import com.samuelribeiro.recorda.data.source.local.SyncCommandDao
import com.samuelribeiro.recorda.data.source.local.SyncCommandEntity
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncCommandDispatcherTest {

    private val syncCommandDao: SyncCommandDao = mockk(relaxed = true)
    private val syncScheduler: SyncScheduler = mockk(relaxed = true)
    private val dispatcher = SyncCommandDispatcher(syncCommandDao, Gson(), syncScheduler)

    @Test
    fun `enqueue inserts a PENDING command with serialized payload`() = runTest {
        val entity = slot<SyncCommandEntity>()

        dispatcher.enqueue(SyncCommandType.CREATE_TOPIC, "t1", CreateTopicPayload("t1", "Kotlin"))

        coVerify { syncCommandDao.insert(capture(entity)) }
        val captured = entity.captured
        assertEquals("CREATE_TOPIC", captured.commandType)
        assertEquals("t1", captured.entityId)
        assertEquals("PENDING", captured.status)
        assertTrue(captured.id.isNotBlank())
        assertTrue(captured.payloadJson.contains("Kotlin"))
    }

    @Test
    fun `enqueue requests an opportunistic sync after inserting`() = runTest {
        dispatcher.enqueue(SyncCommandType.DELETE_TOPIC, "t1", DeleteTopicPayload("t1"))

        verify { syncScheduler.requestSync() }
    }
}

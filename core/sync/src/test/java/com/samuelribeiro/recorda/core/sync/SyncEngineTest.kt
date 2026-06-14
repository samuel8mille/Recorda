package com.samuelribeiro.recorda.core.sync

import com.samuelribeiro.recorda.core.sync.remote.PushCommandsResponse
import com.samuelribeiro.recorda.core.sync.remote.SyncApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SyncEngineTest {

    private val syncQueue: SyncQueue = mockk(relaxed = true)
    private val syncApi: SyncApi = mockk()
    private val deviceIdProvider: DeviceIdProvider = mockk()

    private val engine = SyncEngine(syncQueue, syncApi, deviceIdProvider)

    private fun command(id: String) = SyncCommand(id, "CREATE_TOPIC", "t1", "{}", 1L)

    private fun httpException(code: Int) =
        HttpException(Response.error<Any>(code, "".toResponseBody("text/plain".toMediaType())))

    @Test
    fun `drains all pending commands and marks accepted ones SENT`() = runTest {
        coEvery { deviceIdProvider.ensureRegistered(syncApi) } returns "device-1"
        coEvery { syncQueue.pending(any()) } returnsMany listOf(
            listOf(command("a"), command("b")),
            emptyList(),
        )
        coEvery { syncApi.pushCommands(any(), any()) } returns PushCommandsResponse(listOf("a", "b"))

        val outcome = engine.drain(attemptNumber = 0)

        assertEquals(SyncEngine.Outcome.SUCCESS, outcome)
        coVerify { syncApi.pushCommands("device-1", any()) }
        coVerify { syncQueue.markSent(listOf("a", "b"), any()) }
        coVerify { syncQueue.pruneSent(any()) }
    }

    @Test
    fun `marks only backend-accepted ids as SENT`() = runTest {
        coEvery { deviceIdProvider.ensureRegistered(syncApi) } returns "device-1"
        coEvery { syncQueue.pending(any()) } returnsMany listOf(listOf(command("a"), command("b")), emptyList())
        coEvery { syncApi.pushCommands(any(), any()) } returns PushCommandsResponse(listOf("a"))

        engine.drain(attemptNumber = 0)

        coVerify { syncQueue.markSent(listOf("a"), any()) }
    }

    @Test
    fun `network failure keeps commands PENDING and requests retry`() = runTest {
        coEvery { deviceIdProvider.ensureRegistered(syncApi) } returns "device-1"
        coEvery { syncQueue.pending(any()) } returns listOf(command("a"))
        coEvery { syncApi.pushCommands(any(), any()) } throws IOException("offline")

        val outcome = engine.drain(attemptNumber = 0)

        assertEquals(SyncEngine.Outcome.RETRY, outcome)
        coVerify { syncQueue.markFailed(listOf("a"), "PENDING", "offline", any()) }
        coVerify(exactly = 0) { syncQueue.pruneSent(any()) }
    }

    @Test
    fun `network failure on last attempt marks commands FAILED`() = runTest {
        coEvery { deviceIdProvider.ensureRegistered(syncApi) } returns "device-1"
        coEvery { syncQueue.pending(any()) } returns listOf(command("a"))
        coEvery { syncApi.pushCommands(any(), any()) } throws IOException("offline")

        val outcome = engine.drain(attemptNumber = 4)

        assertEquals(SyncEngine.Outcome.RETRY, outcome)
        coVerify { syncQueue.markFailed(listOf("a"), "FAILED", "offline", any()) }
    }

    @Test
    fun `4xx marks the batch FAILED and continues draining`() = runTest {
        coEvery { deviceIdProvider.ensureRegistered(syncApi) } returns "device-1"
        coEvery { syncQueue.pending(any()) } returnsMany listOf(listOf(command("a")), emptyList())
        coEvery { syncApi.pushCommands(any(), any()) } throws httpException(400)

        val outcome = engine.drain(attemptNumber = 0)

        assertEquals(SyncEngine.Outcome.SUCCESS, outcome)
        coVerify { syncQueue.markFailed(listOf("a"), "FAILED", any(), any()) }
        coVerify { syncQueue.pruneSent(any()) }
    }

    @Test
    fun `5xx keeps commands PENDING and requests retry`() = runTest {
        coEvery { deviceIdProvider.ensureRegistered(syncApi) } returns "device-1"
        coEvery { syncQueue.pending(any()) } returns listOf(command("a"))
        coEvery { syncApi.pushCommands(any(), any()) } throws httpException(503)

        val outcome = engine.drain(attemptNumber = 0)

        assertEquals(SyncEngine.Outcome.RETRY, outcome)
        coVerify { syncQueue.markFailed(listOf("a"), "PENDING", any(), any()) }
    }

    @Test
    fun `registration failure over network requests retry without touching queue`() = runTest {
        coEvery { deviceIdProvider.ensureRegistered(syncApi) } throws IOException("offline")

        val outcome = engine.drain(attemptNumber = 0)

        assertEquals(SyncEngine.Outcome.RETRY, outcome)
        coVerify(exactly = 0) { syncQueue.pending(any()) }
    }

    @Test
    fun `empty queue succeeds and prunes without uploading`() = runTest {
        coEvery { deviceIdProvider.ensureRegistered(syncApi) } returns "device-1"
        coEvery { syncQueue.pending(any()) } returns emptyList()

        val outcome = engine.drain(attemptNumber = 0)

        assertEquals(SyncEngine.Outcome.SUCCESS, outcome)
        coVerify(exactly = 0) { syncApi.pushCommands(any(), any()) }
        coVerify { syncQueue.pruneSent(any()) }
    }
}

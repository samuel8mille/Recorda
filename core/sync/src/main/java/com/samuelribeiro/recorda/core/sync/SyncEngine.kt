package com.samuelribeiro.recorda.core.sync

import com.samuelribeiro.recorda.core.sync.remote.PushCommandsRequest
import com.samuelribeiro.recorda.core.sync.remote.SyncApi
import com.samuelribeiro.recorda.core.sync.remote.SyncCommandDto
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Pure (Android-free) drain logic shared by [SyncWorker], so it can be unit-tested without
 * Robolectric or WorkManager test infra.
 *
 * Drains the [SyncQueue] to the backend in batches: each batch is uploaded and, on backend
 * acceptance, marked SENT. The [Outcome] tells the worker whether to retry. A single poison
 * command (4xx) is marked FAILED and skipped so it never blocks the rest of the queue.
 */
class SyncEngine @Inject constructor(
    private val syncQueue: SyncQueue,
    private val syncApi: SyncApi,
    private val deviceIdProvider: DeviceIdProvider,
) {
    /** Result of a drain attempt, mapped by the worker to WorkManager's success/retry/failure. */
    enum class Outcome { SUCCESS, RETRY, FAILURE }

    /**
     * Drains all pending commands. [attemptNumber] is the WorkManager run attempt (0-based),
     * used to decide when retries are exhausted and a stuck command should be marked FAILED.
     */
    suspend fun drain(attemptNumber: Int): Outcome {
        val deviceId = try {
            deviceIdProvider.ensureRegistered(syncApi)
        } catch (e: IOException) {
            return Outcome.RETRY
        } catch (e: HttpException) {
            return if (e.code() in CLIENT_ERROR_RANGE) Outcome.FAILURE else Outcome.RETRY
        }

        while (true) {
            val batch = syncQueue.pending(BATCH_SIZE)
            if (batch.isEmpty()) break

            try {
                val accepted = syncApi.pushCommands(deviceId, batch.toRequest()).acceptedIds
                syncQueue.markSent(accepted, now())
            } catch (e: IOException) {
                syncQueue.markFailed(batch.ids(), retryStatus(attemptNumber), e.message, now())
                return Outcome.RETRY
            } catch (e: HttpException) {
                if (e.code() in CLIENT_ERROR_RANGE) {
                    syncQueue.markFailed(batch.ids(), STATUS_FAILED, e.message, now())
                } else {
                    syncQueue.markFailed(batch.ids(), retryStatus(attemptNumber), e.message, now())
                    return Outcome.RETRY
                }
            }
        }

        syncQueue.pruneSent(now() - PRUNE_AFTER_MILLIS)
        return Outcome.SUCCESS
    }

    private fun List<SyncCommand>.toRequest() = PushCommandsRequest(
        commands = map { SyncCommandDto(it.id, it.commandType, it.entityId, it.payloadJson, it.createdAtMillis) },
    )

    private fun List<SyncCommand>.ids() = map { it.id }

    private fun retryStatus(attemptNumber: Int): String =
        if (attemptNumber + 1 >= MAX_RETRIES) STATUS_FAILED else STATUS_PENDING

    private fun now() = System.currentTimeMillis()

    private companion object {
        const val BATCH_SIZE = 50
        const val MAX_RETRIES = 5
        const val PRUNE_AFTER_MILLIS = 7L * 24 * 60 * 60 * 1000
        const val STATUS_PENDING = "PENDING"
        const val STATUS_FAILED = "FAILED"
        val CLIENT_ERROR_RANGE = 400..499
    }
}

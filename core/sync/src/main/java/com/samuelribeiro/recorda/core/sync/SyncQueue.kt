package com.samuelribeiro.recorda.core.sync

/**
 * A single local mutation awaiting upload to the sync backend.
 *
 * Mirrors the persisted command without binding this module to Room: the `:app` module
 * provides a [SyncQueue] adapter over its own `sync_commands` table.
 */
data class SyncCommand(
    val id: String,
    val commandType: String,
    val entityId: String,
    val payloadJson: String,
    val createdAtMillis: Long,
)

/**
 * Storage-agnostic contract over the local command queue, consumed by [SyncWorker].
 *
 * Implemented in `:app` by an adapter that delegates to the Room `SyncCommandDao`, so this
 * module never depends on Room.
 */
interface SyncQueue {

    /** Returns up to [limit] oldest PENDING commands, oldest first. */
    suspend fun pending(limit: Int): List<SyncCommand>

    /** Marks the given commands as SENT, stamping [attemptAtMillis] as the last attempt time. */
    suspend fun markSent(ids: List<String>, attemptAtMillis: Long)

    /**
     * Records a failed upload attempt for the given commands, setting their [status]
     * (kept PENDING for retries, or FAILED once exhausted), incrementing the retry count
     * and storing [error] and [attemptAtMillis].
     */
    suspend fun markFailed(ids: List<String>, status: String, error: String?, attemptAtMillis: Long)

    /** Deletes SENT commands whose last attempt is older than [olderThanMillis]. */
    suspend fun pruneSent(olderThanMillis: Long)
}

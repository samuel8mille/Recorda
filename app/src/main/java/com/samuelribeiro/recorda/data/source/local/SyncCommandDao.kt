package com.samuelribeiro.recorda.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Room DAO for the local offline-first sync command queue. */
@Dao
interface SyncCommandDao {

    /** Enqueues a new [SyncCommandEntity]. */
    @Insert
    suspend fun insert(entity: SyncCommandEntity)

    /** Returns up to [limit] pending commands, oldest first. */
    @Query("SELECT * FROM sync_commands WHERE status = 'PENDING' ORDER BY createdAtMillis ASC LIMIT :limit")
    suspend fun getPendingBatch(limit: Int): List<SyncCommandEntity>

    /** Emits the number of commands awaiting upload. */
    @Query("SELECT COUNT(*) FROM sync_commands WHERE status = 'PENDING'")
    fun pendingCount(): Flow<Int>

    /** Marks the given commands as successfully uploaded. */
    @Query("UPDATE sync_commands SET status = 'SENT', lastAttemptAtMillis = :attemptAtMillis WHERE id IN (:ids)")
    suspend fun markSent(ids: List<String>, attemptAtMillis: Long)

    /** Records a failed upload attempt, bumping the retry count and setting the new [status]. */
    @Query(
        "UPDATE sync_commands SET status = :status, retryCount = retryCount + 1, " +
            "lastErrorMessage = :error, lastAttemptAtMillis = :attemptAtMillis WHERE id IN (:ids)",
    )
    suspend fun markFailed(ids: List<String>, status: String, error: String?, attemptAtMillis: Long)

    /** Deletes uploaded commands whose last attempt is older than [olderThanMillis]. */
    @Query("DELETE FROM sync_commands WHERE status = 'SENT' AND lastAttemptAtMillis < :olderThanMillis")
    suspend fun pruneSent(olderThanMillis: Long)
}

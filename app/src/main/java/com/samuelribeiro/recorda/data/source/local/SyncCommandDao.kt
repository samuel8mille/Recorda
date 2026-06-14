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
}

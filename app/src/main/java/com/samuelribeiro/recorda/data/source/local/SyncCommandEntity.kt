package com.samuelribeiro.recorda.data.source.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A locally-queued mutation pending upload to the sync backend.
 *
 * Each row is an idempotent unit of work: [id] is a client-generated UUID that the
 * backend can use as a deduplication key for retried uploads.
 */
@Entity(tableName = "sync_commands", indices = [Index("status"), Index("entityId")])
data class SyncCommandEntity(
    @PrimaryKey val id: String,
    val commandType: String,
    val entityId: String,
    val payloadJson: String,
    val createdAtMillis: Long,
    val status: String = "PENDING",
    val retryCount: Int = 0,
    val lastErrorMessage: String? = null,
    val lastAttemptAtMillis: Long? = null,
)

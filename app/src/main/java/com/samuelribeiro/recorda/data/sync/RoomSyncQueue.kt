package com.samuelribeiro.recorda.data.sync

import com.samuelribeiro.recorda.core.sync.SyncCommand
import com.samuelribeiro.recorda.core.sync.SyncQueue
import com.samuelribeiro.recorda.data.source.local.SyncCommandDao
import com.samuelribeiro.recorda.data.source.local.SyncCommandEntity
import javax.inject.Inject

/**
 * [SyncQueue] adapter backed by the Room [SyncCommandDao], keeping Room out of `:core:sync`.
 */
class RoomSyncQueue @Inject constructor(
    private val syncCommandDao: SyncCommandDao,
) : SyncQueue {

    override suspend fun pending(limit: Int): List<SyncCommand> =
        syncCommandDao.getPendingBatch(limit).map { it.toSyncCommand() }

    override suspend fun markSent(ids: List<String>, attemptAtMillis: Long) =
        syncCommandDao.markSent(ids, attemptAtMillis)

    override suspend fun markFailed(ids: List<String>, status: String, error: String?, attemptAtMillis: Long) =
        syncCommandDao.markFailed(ids, status, error, attemptAtMillis)

    override suspend fun pruneSent(olderThanMillis: Long) =
        syncCommandDao.pruneSent(olderThanMillis)

    private fun SyncCommandEntity.toSyncCommand() = SyncCommand(
        id = id,
        commandType = commandType,
        entityId = entityId,
        payloadJson = payloadJson,
        createdAtMillis = createdAtMillis,
    )
}

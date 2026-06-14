package com.samuelribeiro.recorda.data.sync

import com.google.gson.Gson
import com.samuelribeiro.recorda.data.source.local.SyncCommandDao
import com.samuelribeiro.recorda.data.source.local.SyncCommandEntity
import java.util.UUID
import javax.inject.Inject

/**
 * Enqueues local mutations as [SyncCommandEntity] rows for later upload to the sync backend.
 *
 * Repositories call [enqueue] after a write has already succeeded locally, so the queue
 * only ever reflects state that is durably persisted in Room.
 */
class SyncCommandDispatcher @Inject constructor(
    private val syncCommandDao: SyncCommandDao,
    private val gson: Gson,
) {
    /**
     * Serializes [payload] to JSON and inserts a PENDING command of the given [type] for
     * [entityId], generating a fresh UUID as the idempotency key.
     */
    suspend fun enqueue(type: SyncCommandType, entityId: String, payload: Any) {
        syncCommandDao.insert(
            SyncCommandEntity(
                id = UUID.randomUUID().toString(),
                commandType = type.name,
                entityId = entityId,
                payloadJson = gson.toJson(payload),
                createdAtMillis = System.currentTimeMillis(),
            ),
        )
    }
}

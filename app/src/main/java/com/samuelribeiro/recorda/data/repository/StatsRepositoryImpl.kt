package com.samuelribeiro.recorda.data.repository

import com.samuelribeiro.recorda.data.source.local.ReviewLogDao
import com.samuelribeiro.recorda.data.source.local.ReviewLogEntity
import com.samuelribeiro.recorda.data.sync.DeleteReviewLogPayload
import com.samuelribeiro.recorda.data.sync.LogReviewPayload
import com.samuelribeiro.recorda.data.sync.SyncCommandDispatcher
import com.samuelribeiro.recorda.data.sync.SyncCommandType
import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.ReviewLogEntry
import com.samuelribeiro.recorda.domain.repository.StatsRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Room-backed implementation of [StatsRepository].
 *
 * Ratings are persisted by enum name and restored with [CardRating.valueOf] — safe
 * because only this app writes the log and schema upgrades are destructive.
 */
class StatsRepositoryImpl @Inject constructor(
    private val reviewLogDao: ReviewLogDao,
    private val syncCommandDispatcher: SyncCommandDispatcher,
) : StatsRepository {

    override suspend fun logReview(topicId: String, entry: ReviewLogEntry) {
        reviewLogDao.insert(
            ReviewLogEntity(
                topicId = topicId,
                cardIndex = entry.cardIndex,
                rating = entry.rating.name,
                timestampMillis = entry.timestampMillis,
            ),
        )
        syncCommandDispatcher.enqueue(
            SyncCommandType.LOG_REVIEW,
            UUID.randomUUID().toString(),
            LogReviewPayload(
                topicId = topicId,
                cardIndex = entry.cardIndex,
                rating = entry.rating.name,
                timestampMillis = entry.timestampMillis,
            ),
        )
    }

    override suspend fun getReviewLog(topicId: String): List<ReviewLogEntry> =
        reviewLogDao.getLogsForTopic(topicId).map { entity ->
            ReviewLogEntry(
                cardIndex = entity.cardIndex,
                rating = CardRating.valueOf(entity.rating),
                timestampMillis = entity.timestampMillis,
            )
        }

    override suspend fun deleteReviewLog(topicId: String) {
        reviewLogDao.deleteByTopicId(topicId)
        syncCommandDispatcher.enqueue(
            SyncCommandType.DELETE_REVIEW_LOG,
            topicId,
            DeleteReviewLogPayload(topicId),
        )
    }
}

package com.samuelribeiro.recorda.data.repository

import com.samuelribeiro.recorda.data.source.local.FlashcardReviewDao
import com.samuelribeiro.recorda.data.source.local.FlashcardReviewEntity
import com.samuelribeiro.recorda.data.sync.SaveReviewStatePayload
import com.samuelribeiro.recorda.data.sync.SyncCommandDispatcher
import com.samuelribeiro.recorda.data.sync.SyncCommandType
import com.samuelribeiro.recorda.domain.model.FlashcardReviewState
import com.samuelribeiro.recorda.domain.repository.ReviewRepository
import javax.inject.Inject

/** Room-backed implementation of [ReviewRepository]. */
class ReviewRepositoryImpl @Inject constructor(
    private val dao: FlashcardReviewDao,
    private val syncCommandDispatcher: SyncCommandDispatcher,
) : ReviewRepository {

    override suspend fun getReviewStates(topicId: String): List<FlashcardReviewState> =
        dao.getReviewsForTopic(topicId).map { it.toDomain() }

    override suspend fun deleteReviewStates(topicId: String) {
        dao.deleteByTopicId(topicId)
    }

    override suspend fun saveReviewState(topicId: String, state: FlashcardReviewState) {
        val updatedAtMillis = System.currentTimeMillis()
        dao.upsert(
            FlashcardReviewEntity(
                id = "${topicId}_${state.cardIndex}",
                topicId = topicId,
                cardIndex = state.cardIndex,
                easeFactor = state.easeFactor,
                intervalDays = state.intervalDays,
                repetitions = state.repetitions,
                nextReviewAtMillis = state.nextReviewAtMillis,
                updatedAtMillis = updatedAtMillis,
            )
        )
        syncCommandDispatcher.enqueue(
            SyncCommandType.SAVE_REVIEW_STATE,
            "${topicId}_${state.cardIndex}",
            SaveReviewStatePayload(
                topicId = topicId,
                cardIndex = state.cardIndex,
                easeFactor = state.easeFactor,
                intervalDays = state.intervalDays,
                repetitions = state.repetitions,
                nextReviewAtMillis = state.nextReviewAtMillis,
                updatedAtMillis = updatedAtMillis,
            ),
        )
    }

    private fun FlashcardReviewEntity.toDomain() = FlashcardReviewState(
        cardIndex = cardIndex,
        easeFactor = easeFactor,
        intervalDays = intervalDays,
        repetitions = repetitions,
        nextReviewAtMillis = nextReviewAtMillis,
    )
}

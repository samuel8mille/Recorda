package com.samuelribeiro.recorda.domain.repository

import com.samuelribeiro.recorda.domain.model.FlashcardReviewState

/** Contract for persisting and loading per-card spaced-repetition state. */
interface ReviewRepository {

    /** Returns the saved [FlashcardReviewState] for every reviewed card in [topicId]. */
    suspend fun getReviewStates(topicId: String): List<FlashcardReviewState>

    /** Upserts the scheduling state for a single card identified by [topicId] + [state.cardIndex]. */
    suspend fun saveReviewState(topicId: String, state: FlashcardReviewState)

    /** Deletes all review states associated with [topicId]. */
    suspend fun deleteReviewStates(topicId: String)
}

package com.samuelribeiro.recorda.domain.repository

import com.samuelribeiro.recorda.domain.model.ReviewLogEntry

/**
 * Contract for persisting and reading the append-only review event log.
 */
interface StatsRepository {

    /** Appends [entry] to the review log of the topic with [topicId]. */
    suspend fun logReview(topicId: String, entry: ReviewLogEntry)

    /** Returns all review events of the topic with [topicId], oldest first. */
    suspend fun getReviewLog(topicId: String): List<ReviewLogEntry>

    /** Deletes the review log of the topic with [topicId]. */
    suspend fun deleteReviewLog(topicId: String)
}

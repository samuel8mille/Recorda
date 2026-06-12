package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.repository.ReviewRepository
import com.samuelribeiro.recorda.domain.repository.StatsRepository
import com.samuelribeiro.recorda.domain.repository.TopicRepository

/**
 * Permanently removes a topic, its review states and its review log.
 *
 * @param topicRepository Handles topic persistence.
 * @param reviewRepository Handles per-card review state persistence.
 * @param statsRepository Handles the append-only review event log.
 */
class DeleteTopicUseCase(
    private val topicRepository: TopicRepository,
    private val reviewRepository: ReviewRepository,
    private val statsRepository: StatsRepository,
) {
    /** Deletes the topic identified by [topicId], its SM-2 review states and its review log. */
    suspend operator fun invoke(topicId: String) {
        reviewRepository.deleteReviewStates(topicId)
        statsRepository.deleteReviewLog(topicId)
        topicRepository.deleteTopic(topicId)
    }
}

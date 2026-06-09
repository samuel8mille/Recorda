package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.repository.ReviewRepository
import com.samuelribeiro.recorda.domain.repository.TopicRepository

/**
 * Permanently removes a topic and all its associated review states.
 *
 * @param topicRepository Handles topic persistence.
 * @param reviewRepository Handles per-card review state persistence.
 */
class DeleteTopicUseCase(
    private val topicRepository: TopicRepository,
    private val reviewRepository: ReviewRepository,
) {
    /** Deletes the topic identified by [topicId] and all its SM-2 review states. */
    suspend operator fun invoke(topicId: String) {
        reviewRepository.deleteReviewStates(topicId)
        topicRepository.deleteTopic(topicId)
    }
}

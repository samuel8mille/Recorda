package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.TopicStats
import com.samuelribeiro.recorda.domain.repository.ReviewRepository
import com.samuelribeiro.recorda.domain.repository.StatsRepository
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.stats.TopicStatsCalculator
import kotlinx.coroutines.flow.first

/**
 * Loads everything needed to compute a topic's retention statistics and aggregates it.
 *
 * @param topicRepository Source of the topic and its card count.
 * @param reviewRepository Source of the current SM-2 state per card.
 * @param statsRepository Source of the append-only review log.
 * @param calculator Pure aggregation of the loaded data.
 */
class GetTopicStatsUseCase(
    private val topicRepository: TopicRepository,
    private val reviewRepository: ReviewRepository,
    private val statsRepository: StatsRepository,
    private val calculator: TopicStatsCalculator,
) {

    /**
     * Computes the [TopicStats] of the topic with [topicId]. A snapshot is enough: the
     * log only changes inside the review session, never while the stats screen is open.
     */
    suspend operator fun invoke(topicId: String): TopicStats {
        val topic = topicRepository.getTopic(topicId).first()
        return calculator.calculate(
            totalCards = topic?.flashcards?.size ?: 0,
            reviewStates = reviewRepository.getReviewStates(topicId),
            logs = statsRepository.getReviewLog(topicId),
        )
    }
}

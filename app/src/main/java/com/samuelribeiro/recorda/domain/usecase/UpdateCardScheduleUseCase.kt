package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.FlashcardReviewState
import com.samuelribeiro.recorda.domain.model.ReviewLogEntry
import com.samuelribeiro.recorda.domain.repository.ReviewRepository
import com.samuelribeiro.recorda.domain.repository.StatsRepository
import com.samuelribeiro.recorda.domain.scheduler.ReviewScheduler

/**
 * Applies SM-2 scheduling to a rated card, persists the updated state and appends
 * the review event to the retention log.
 *
 * Encapsulates the "schedule + save + log" workflow so the ViewModel does not depend
 * on [ReviewScheduler], [ReviewRepository] or [StatsRepository] directly — and so a
 * review can never be scheduled without being logged.
 */
class UpdateCardScheduleUseCase(
    private val scheduler: ReviewScheduler,
    private val repository: ReviewRepository,
    private val statsRepository: StatsRepository,
) {

    /**
     * Schedules [state] based on [rating], persists the result, logs the review event,
     * and returns the updated state.
     *
     * @param topicId Identifies which topic the card belongs to.
     * @param state Current SM-2 state for the card being rated.
     * @param rating User's self-assessment of recall quality.
     */
    suspend operator fun invoke(
        topicId: String,
        state: FlashcardReviewState,
        rating: CardRating,
    ): FlashcardReviewState {
        val result = scheduler.schedule(
            easeFactor = state.easeFactor,
            intervalDays = state.intervalDays,
            repetitions = state.repetitions,
            rating = rating,
        )
        val now = System.currentTimeMillis()
        val updated = FlashcardReviewState(
            cardIndex = state.cardIndex,
            easeFactor = result.newEaseFactor,
            intervalDays = result.newIntervalDays,
            repetitions = result.newRepetitions,
            nextReviewAtMillis = now + result.newIntervalDays * MILLIS_PER_DAY,
        )
        repository.saveReviewState(topicId, updated)
        statsRepository.logReview(
            topicId = topicId,
            entry = ReviewLogEntry(cardIndex = state.cardIndex, rating = rating, timestampMillis = now),
        )
        return updated
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}

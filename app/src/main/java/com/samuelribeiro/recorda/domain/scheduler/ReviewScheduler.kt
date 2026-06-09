package com.samuelribeiro.recorda.domain.scheduler

import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.ReviewResult

/**
 * Contract for spaced-repetition scheduling algorithms.
 *
 * Implementations (e.g. SM-2, FSRS) are swappable behind this interface
 * following the Swap Pattern used throughout this project.
 */
interface ReviewScheduler {

    /**
     * Calculates the next review schedule for a card after the user rates it.
     *
     * @param easeFactor Current SM-2 ease factor (≥ 1.3).
     * @param intervalDays Current review interval in days.
     * @param repetitions Current consecutive-correct count.
     * @param rating User's self-assessment of recall quality.
     * @return Updated scheduling parameters.
     */
    fun schedule(
        easeFactor: Float,
        intervalDays: Int,
        repetitions: Int,
        rating: CardRating,
    ): ReviewResult
}

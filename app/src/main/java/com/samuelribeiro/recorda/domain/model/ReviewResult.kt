package com.samuelribeiro.recorda.domain.model

/**
 * Output of the SM-2 scheduling algorithm for a single card rating.
 *
 * @property newEaseFactor Updated ease factor after applying the rating.
 * @property newIntervalDays Days until the next review.
 * @property newRepetitions Updated consecutive-correct-recall counter.
 */
data class ReviewResult(
    val newEaseFactor: Float,
    val newIntervalDays: Int,
    val newRepetitions: Int,
)

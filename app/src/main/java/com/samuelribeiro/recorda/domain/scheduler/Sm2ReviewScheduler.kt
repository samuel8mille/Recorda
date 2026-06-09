package com.samuelribeiro.recorda.domain.scheduler

import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.ReviewResult
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * SuperMemo 2 (SM-2) implementation of [ReviewScheduler].
 *
 * Maps [CardRating] to the 0–5 quality scale defined by the SM-2 spec:
 *   AGAIN → 0 (blackout, reset interval)
 *   GOOD  → 3 (correct with significant difficulty)
 *   EASY  → 5 (perfect recall)
 */
class Sm2ReviewScheduler @Inject constructor() : ReviewScheduler {

    override fun schedule(
        easeFactor: Float,
        intervalDays: Int,
        repetitions: Int,
        rating: CardRating,
    ): ReviewResult {
        val quality = rating.toQuality()

        return if (quality < PASSING_QUALITY) {
            ReviewResult(
                newEaseFactor = easeFactor,
                newIntervalDays = 1,
                newRepetitions = 0,
            )
        } else {
            val newInterval = when (repetitions) {
                0 -> 1
                1 -> 6
                else -> (intervalDays * easeFactor).roundToInt()
            }
            val ef = easeFactor + 0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f)
            ReviewResult(
                newEaseFactor = max(MIN_EASE_FACTOR, ef),
                newIntervalDays = newInterval,
                newRepetitions = repetitions + 1,
            )
        }
    }

    private fun CardRating.toQuality(): Int = when (this) {
        CardRating.AGAIN -> 0
        CardRating.GOOD -> 3
        CardRating.EASY -> 5
    }

    private companion object {
        const val PASSING_QUALITY = 3
        const val MIN_EASE_FACTOR = 1.3f
    }
}

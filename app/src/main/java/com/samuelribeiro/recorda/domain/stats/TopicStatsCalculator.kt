package com.samuelribeiro.recorda.domain.stats

import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.DailyReviewCount
import com.samuelribeiro.recorda.domain.model.FlashcardReviewState
import com.samuelribeiro.recorda.domain.model.ReviewLogEntry
import com.samuelribeiro.recorda.domain.model.TopicStats
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

/**
 * Pure aggregation of retention statistics from review state and the review log.
 *
 * All day-based metrics convert timestamps to the local calendar day of [clock]'s
 * time zone — never UTC buckets — so reviews late at night land on the right day.
 * The streak counts consecutive days with at least one review ending today; when
 * today has no review yet, it starts counting from yesterday instead of zeroing
 * the streak mid-day.
 *
 * @param clock Source of "now" and time zone; inject a fixed clock in tests.
 */
class TopicStatsCalculator(
    private val clock: Clock,
) {

    /**
     * Computes the [TopicStats] of a topic with [totalCards] cards from its current
     * [reviewStates] and full review [logs].
     */
    fun calculate(
        totalCards: Int,
        reviewStates: List<FlashcardReviewState>,
        logs: List<ReviewLogEntry>,
    ): TopicStats {
        val now = clock.millis()
        val cardsDue = reviewStates.count { it.nextReviewAtMillis <= now }
        val reviewDays = logs.groupingBy { it.toLocalDate() }.eachCount()
        return TopicStats(
            totalCards = totalCards,
            cardsOnTrack = reviewStates.size - cardsDue,
            cardsDue = cardsDue,
            cardsNeverReviewed = (totalCards - reviewStates.size).coerceAtLeast(0),
            successRate = successRate(logs),
            reviewsPerDay = lastSevenDays(reviewDays),
            streakDays = streak(reviewDays.keys),
            averageEaseFactor = reviewStates.map { it.easeFactor }.average()
                .takeIf { reviewStates.isNotEmpty() }?.toFloat(),
        )
    }

    private fun successRate(logs: List<ReviewLogEntry>): Float? {
        if (logs.isEmpty()) return null
        val passed = logs.count { it.rating == CardRating.GOOD || it.rating == CardRating.EASY }
        return passed.toFloat() / logs.size
    }

    private fun lastSevenDays(reviewDays: Map<LocalDate, Int>): List<DailyReviewCount> {
        val today = LocalDate.now(clock)
        return (DAYS_IN_CHART - 1 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            DailyReviewCount(date = day, count = reviewDays[day] ?: 0)
        }
    }

    private fun streak(daysWithReviews: Set<LocalDate>): Int {
        val today = LocalDate.now(clock)
        var day = if (today in daysWithReviews) today else today.minusDays(1)
        var count = 0
        while (day in daysWithReviews) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    private fun ReviewLogEntry.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(timestampMillis).atZone(clock.zone).toLocalDate()

    private companion object {
        const val DAYS_IN_CHART = 7
    }
}

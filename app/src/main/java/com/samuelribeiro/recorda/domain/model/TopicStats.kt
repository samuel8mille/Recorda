package com.samuelribeiro.recorda.domain.model

import java.time.LocalDate

/**
 * Aggregated retention statistics of a topic.
 *
 * @property totalCards Total number of flashcards in the topic.
 * @property cardsOnTrack Cards whose next review is still in the future ("em dia").
 * @property cardsDue Cards due for review now (next review at or before the current time).
 * @property cardsNeverReviewed Cards that were never reviewed.
 * @property successRate Fraction of GOOD/EASY reviews over all reviews ever logged, or `null`
 * when the topic was never reviewed.
 * @property reviewsPerDay Review counts of the last 7 days, oldest day first.
 * @property streakDays Consecutive days with at least one review, counting back from today
 * (a day without reviews **today** does not break the streak until midnight).
 * @property averageEaseFactor Mean SM-2 ease factor of the reviewed cards, or `null` when no
 * card was reviewed yet.
 */
data class TopicStats(
    val totalCards: Int,
    val cardsOnTrack: Int,
    val cardsDue: Int,
    val cardsNeverReviewed: Int,
    val successRate: Float?,
    val reviewsPerDay: List<DailyReviewCount>,
    val streakDays: Int,
    val averageEaseFactor: Float?,
)

/**
 * Number of reviews performed on a given day.
 *
 * @property date The local calendar day.
 * @property count How many reviews happened on [date].
 */
data class DailyReviewCount(
    val date: LocalDate,
    val count: Int,
)

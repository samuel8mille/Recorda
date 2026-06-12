package com.samuelribeiro.recorda.domain.stats

import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.FlashcardReviewState
import com.samuelribeiro.recorda.domain.model.ReviewLogEntry
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TopicStatsCalculatorTest {

    private val zone = ZoneId.of("America/Sao_Paulo")
    private val fixedInstant = Instant.parse("2026-06-10T15:00:00Z")
    private val clock = Clock.fixed(fixedInstant, zone)
    private val calculator = TopicStatsCalculator(clock)

    private val today: LocalDate = LocalDate.now(clock)

    private fun logAt(day: LocalDate, rating: CardRating = CardRating.GOOD): ReviewLogEntry =
        ReviewLogEntry(
            cardIndex = 0,
            rating = rating,
            timestampMillis = day.atStartOfDay(zone).plusHours(12).toInstant().toEpochMilli(),
        )

    @Test
    fun `empty topic yields null rates, zero streak and empty buckets`() {
        val stats = calculator.calculate(totalCards = 5, reviewStates = emptyList(), logs = emptyList())

        assertNull(stats.successRate)
        assertNull(stats.averageEaseFactor)
        assertEquals(0, stats.streakDays)
        assertEquals(5, stats.cardsNeverReviewed)
        assertEquals(0, stats.cardsOnTrack)
        assertEquals(0, stats.cardsDue)
        assertEquals(7, stats.reviewsPerDay.size)
        assertTrue(stats.reviewsPerDay.all { it.count == 0 })
    }

    @Test
    fun `success rate counts GOOD and EASY over all logs`() {
        val logs = listOf(
            logAt(today, CardRating.GOOD),
            logAt(today, CardRating.EASY),
            logAt(today, CardRating.AGAIN),
            logAt(today, CardRating.AGAIN),
        )

        val stats = calculator.calculate(totalCards = 1, reviewStates = emptyList(), logs = logs)

        assertEquals(0.5f, stats.successRate)
    }

    @Test
    fun `cards due and on track split on nextReviewAtMillis with boundary as due`() {
        val now = clock.millis()
        val states = listOf(
            FlashcardReviewState(cardIndex = 0, nextReviewAtMillis = now),
            FlashcardReviewState(cardIndex = 1, nextReviewAtMillis = now - 1),
            FlashcardReviewState(cardIndex = 2, nextReviewAtMillis = now + 1),
        )

        val stats = calculator.calculate(totalCards = 4, reviewStates = states, logs = emptyList())

        assertEquals(2, stats.cardsDue)
        assertEquals(1, stats.cardsOnTrack)
        assertEquals(1, stats.cardsNeverReviewed)
    }

    @Test
    fun `average ease factor is mean of states`() {
        val states = listOf(
            FlashcardReviewState(cardIndex = 0, easeFactor = 2.0f),
            FlashcardReviewState(cardIndex = 1, easeFactor = 3.0f),
        )

        val stats = calculator.calculate(totalCards = 2, reviewStates = states, logs = emptyList())

        assertEquals(2.5f, stats.averageEaseFactor)
    }

    @Test
    fun `last seven days buckets count reviews per local day oldest first`() {
        val logs = listOf(
            logAt(today),
            logAt(today),
            logAt(today.minusDays(2)),
            logAt(today.minusDays(6)),
            logAt(today.minusDays(7)),
        )

        val stats = calculator.calculate(totalCards = 1, reviewStates = emptyList(), logs = logs)

        assertEquals(today.minusDays(6), stats.reviewsPerDay.first().date)
        assertEquals(today, stats.reviewsPerDay.last().date)
        assertEquals(1, stats.reviewsPerDay[0].count)
        assertEquals(1, stats.reviewsPerDay[4].count)
        assertEquals(2, stats.reviewsPerDay[6].count)
    }

    @Test
    fun `streak counts consecutive days ending today`() {
        val logs = listOf(logAt(today), logAt(today.minusDays(1)), logAt(today.minusDays(2)))

        val stats = calculator.calculate(totalCards = 1, reviewStates = emptyList(), logs = logs)

        assertEquals(3, stats.streakDays)
    }

    @Test
    fun `streak is preserved when today has no review yet`() {
        val logs = listOf(logAt(today.minusDays(1)), logAt(today.minusDays(2)))

        val stats = calculator.calculate(totalCards = 1, reviewStates = emptyList(), logs = logs)

        assertEquals(2, stats.streakDays)
    }

    @Test
    fun `streak breaks on a gap day`() {
        val logs = listOf(logAt(today), logAt(today.minusDays(2)), logAt(today.minusDays(3)))

        val stats = calculator.calculate(totalCards = 1, reviewStates = emptyList(), logs = logs)

        assertEquals(1, stats.streakDays)
    }

    @Test
    fun `timestamps convert to local day of the clock zone not utc`() {
        val utcEarlyToday = Instant.parse("2026-06-10T02:00:00Z").toEpochMilli()
        val logs = listOf(ReviewLogEntry(cardIndex = 0, rating = CardRating.GOOD, timestampMillis = utcEarlyToday))

        val stats = calculator.calculate(totalCards = 1, reviewStates = emptyList(), logs = logs)

        val yesterdayBucket = stats.reviewsPerDay.first { it.date == today.minusDays(1) }
        val todayBucket = stats.reviewsPerDay.first { it.date == today }
        assertEquals(1, yesterdayBucket.count)
        assertEquals(0, todayBucket.count)
    }

    @Test
    fun `old logs are out of buckets but count for rate and streak base`() {
        val logs = listOf(logAt(today.minusDays(30), CardRating.AGAIN))

        val stats = calculator.calculate(totalCards = 1, reviewStates = emptyList(), logs = logs)

        assertTrue(stats.reviewsPerDay.all { it.count == 0 })
        assertEquals(0f, stats.successRate)
        assertEquals(0, stats.streakDays)
    }
}

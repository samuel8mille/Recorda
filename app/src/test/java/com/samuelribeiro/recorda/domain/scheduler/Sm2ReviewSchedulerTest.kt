package com.samuelribeiro.recorda.domain.scheduler

import com.samuelribeiro.recorda.domain.model.CardRating
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Sm2ReviewSchedulerTest {

    private val scheduler = Sm2ReviewScheduler()

    @Test
    fun `AGAIN resets repetitions to zero`() {
        val result = scheduler.schedule(
            easeFactor = 2.5f,
            intervalDays = 6,
            repetitions = 3,
            rating = CardRating.AGAIN,
        )

        assertEquals(0, result.newRepetitions)
    }

    @Test
    fun `AGAIN resets interval to 1 day`() {
        val result = scheduler.schedule(
            easeFactor = 2.5f,
            intervalDays = 6,
            repetitions = 3,
            rating = CardRating.AGAIN,
        )

        assertEquals(1, result.newIntervalDays)
    }

    @Test
    fun `AGAIN preserves ease factor`() {
        val result = scheduler.schedule(
            easeFactor = 2.5f,
            intervalDays = 6,
            repetitions = 3,
            rating = CardRating.AGAIN,
        )

        assertEquals(2.5f, result.newEaseFactor)
    }

    @Test
    fun `GOOD on first card sets interval to 1 day`() {
        val result = scheduler.schedule(
            easeFactor = 2.5f,
            intervalDays = 1,
            repetitions = 0,
            rating = CardRating.GOOD,
        )

        assertEquals(1, result.newIntervalDays)
        assertEquals(1, result.newRepetitions)
    }

    @Test
    fun `GOOD on second card sets interval to 6 days`() {
        val result = scheduler.schedule(
            easeFactor = 2.5f,
            intervalDays = 1,
            repetitions = 1,
            rating = CardRating.GOOD,
        )

        assertEquals(6, result.newIntervalDays)
        assertEquals(2, result.newRepetitions)
    }

    @Test
    fun `GOOD on subsequent cards multiplies interval by ease factor`() {
        val result = scheduler.schedule(
            easeFactor = 2.5f,
            intervalDays = 6,
            repetitions = 2,
            rating = CardRating.GOOD,
        )

        assertEquals(15, result.newIntervalDays)
    }

    @Test
    fun `EASY on first card sets interval to 1 day`() {
        val result = scheduler.schedule(
            easeFactor = 2.5f,
            intervalDays = 1,
            repetitions = 0,
            rating = CardRating.EASY,
        )

        assertEquals(1, result.newIntervalDays)
    }

    @Test
    fun `EASY increases ease factor`() {
        val result = scheduler.schedule(
            easeFactor = 2.5f,
            intervalDays = 1,
            repetitions = 0,
            rating = CardRating.EASY,
        )

        assertTrue(result.newEaseFactor > 2.5f)
    }

    @Test
    fun `GOOD decreases ease factor slightly`() {
        val result = scheduler.schedule(
            easeFactor = 2.5f,
            intervalDays = 1,
            repetitions = 0,
            rating = CardRating.GOOD,
        )

        assertTrue(result.newEaseFactor < 2.5f)
    }

    @Test
    fun `ease factor never drops below minimum`() {
        var easeFactor = 2.5f
        var intervalDays = 1
        var repetitions = 0

        repeat(20) {
            val result = scheduler.schedule(
                easeFactor = easeFactor,
                intervalDays = intervalDays,
                repetitions = repetitions,
                rating = CardRating.AGAIN,
            )
            easeFactor = result.newEaseFactor
            intervalDays = result.newIntervalDays
            repetitions = result.newRepetitions
        }

        assertTrue(easeFactor >= 1.3f)
    }

    @Test
    fun `GOOD increments repetitions by one`() {
        val result = scheduler.schedule(
            easeFactor = 2.5f,
            intervalDays = 1,
            repetitions = 5,
            rating = CardRating.GOOD,
        )

        assertEquals(6, result.newRepetitions)
    }
}

package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.FlashcardReviewState
import com.samuelribeiro.recorda.domain.model.ReviewResult
import com.samuelribeiro.recorda.domain.repository.ReviewRepository
import com.samuelribeiro.recorda.domain.scheduler.ReviewScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class UpdateCardScheduleUseCaseTest {

    private val scheduler: ReviewScheduler = mockk()
    private val repository: ReviewRepository = mockk()
    private val useCase = UpdateCardScheduleUseCase(scheduler, repository)

    private val initialState = FlashcardReviewState(
        cardIndex = 2,
        easeFactor = 2.5f,
        intervalDays = 1,
        repetitions = 0,
    )

    @Test
    fun `invoke passes current state fields to scheduler`() = runTest {
        every { scheduler.schedule(any(), any(), any(), any()) } returns ReviewResult(2.5f, 1, 1)
        coEvery { repository.saveReviewState(any(), any()) } returns Unit

        useCase("topic1", initialState, CardRating.GOOD)

        verify {
            scheduler.schedule(
                easeFactor = 2.5f,
                intervalDays = 1,
                repetitions = 0,
                rating = CardRating.GOOD,
            )
        }
    }

    @Test
    fun `invoke saves updated state to repository`() = runTest {
        every { scheduler.schedule(any(), any(), any(), any()) } returns ReviewResult(2.4f, 6, 2)
        coEvery { repository.saveReviewState(any(), any()) } returns Unit

        useCase("topic1", initialState, CardRating.GOOD)

        coVerify {
            repository.saveReviewState(
                topicId = "topic1",
                state = match { it.cardIndex == 2 && it.easeFactor == 2.4f && it.intervalDays == 6 },
            )
        }
    }

    @Test
    fun `invoke returns updated FlashcardReviewState`() = runTest {
        every { scheduler.schedule(any(), any(), any(), any()) } returns ReviewResult(2.6f, 6, 1)
        coEvery { repository.saveReviewState(any(), any()) } returns Unit

        val result = useCase("topic1", initialState, CardRating.EASY)

        assertEquals(2, result.cardIndex)
        assertEquals(2.6f, result.easeFactor)
        assertEquals(6, result.intervalDays)
        assertEquals(1, result.repetitions)
    }

    @Test
    fun `invoke sets nextReviewAtMillis based on interval`() = runTest {
        every { scheduler.schedule(any(), any(), any(), any()) } returns ReviewResult(2.5f, 3, 1)
        coEvery { repository.saveReviewState(any(), any()) } returns Unit

        val before = System.currentTimeMillis()
        val result = useCase("topic1", initialState, CardRating.GOOD)
        val after = System.currentTimeMillis()

        val expectedMin = before + 3 * 86_400_000L
        val expectedMax = after + 3 * 86_400_000L
        assert(result.nextReviewAtMillis in expectedMin..expectedMax)
    }
}

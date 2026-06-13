package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.FlashcardReviewState
import com.samuelribeiro.recorda.domain.repository.ReviewRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetFlashcardReviewsUseCaseTest {

    private val repository: ReviewRepository = mockk()
    private val useCase = GetFlashcardReviewsUseCase(repository)

    @Test
    fun `invoke delegates to repository`() = runTest {
        val states = listOf(FlashcardReviewState(cardIndex = 0, repetitions = 2))
        coEvery { repository.getReviewStates("topic1") } returns states

        val result = useCase("topic1")

        assertEquals(states, result)
        coVerify(exactly = 1) { repository.getReviewStates("topic1") }
    }
}

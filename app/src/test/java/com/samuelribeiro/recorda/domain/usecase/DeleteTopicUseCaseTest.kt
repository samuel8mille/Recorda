package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.repository.ReviewRepository
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteTopicUseCaseTest {

    private val topicRepository: TopicRepository = mockk()
    private val reviewRepository: ReviewRepository = mockk()
    private val useCase = DeleteTopicUseCase(topicRepository, reviewRepository)

    @Test
    fun `invoke deletes review states before topic`() = runTest {
        coEvery { reviewRepository.deleteReviewStates(any()) } returns Unit
        coEvery { topicRepository.deleteTopic(any()) } returns Unit

        useCase("topic1")

        coVerifyOrder {
            reviewRepository.deleteReviewStates("topic1")
            topicRepository.deleteTopic("topic1")
        }
    }

    @Test
    fun `invoke passes correct topicId to deleteReviewStates`() = runTest {
        coEvery { reviewRepository.deleteReviewStates(any()) } returns Unit
        coEvery { topicRepository.deleteTopic(any()) } returns Unit

        useCase("topic42")

        coVerify { reviewRepository.deleteReviewStates("topic42") }
    }

    @Test
    fun `invoke passes correct topicId to deleteTopic`() = runTest {
        coEvery { reviewRepository.deleteReviewStates(any()) } returns Unit
        coEvery { topicRepository.deleteTopic(any()) } returns Unit

        useCase("topic42")

        coVerify { topicRepository.deleteTopic("topic42") }
    }
}

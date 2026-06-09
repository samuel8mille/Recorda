package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetStoredTopicsUseCaseTest {

    private val repository: TopicRepository = mockk()
    private val useCase = GetStoredTopicsUseCase(repository)

    @Test
    fun `invoke delegates to repository`() = runTest {
        val topics = listOf(Topic("1", "Kotlin", listOf(Flashcard("Q?", "A"))))
        every { repository.getStoredTopics() } returns flowOf(topics)

        val results = useCase().toList()

        verify(exactly = 1) { repository.getStoredTopics() }
        assertEquals(listOf(topics), results)
    }

    @Test
    fun `invoke returns empty list when no topics stored`() = runTest {
        every { repository.getStoredTopics() } returns flowOf(emptyList())

        val results = useCase().toList()

        assertTrue(results.first().isEmpty())
    }
}

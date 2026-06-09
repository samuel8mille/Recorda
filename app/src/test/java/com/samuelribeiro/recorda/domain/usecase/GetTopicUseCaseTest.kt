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
import kotlin.test.assertNull

class GetTopicUseCaseTest {

    private val repository: TopicRepository = mockk()
    private val useCase = GetTopicUseCase(repository)

    @Test
    fun `invoke delegates to repository`() = runTest {
        val topic = Topic("1", "Kotlin", listOf(Flashcard("Q?", "A")))
        every { repository.getTopic("1") } returns flowOf(topic)

        val results = useCase("1").toList()

        verify(exactly = 1) { repository.getTopic("1") }
        assertEquals(listOf(topic), results)
    }

    @Test
    fun `invoke returns null when topic not found`() = runTest {
        every { repository.getTopic("missing") } returns flowOf(null)

        val results = useCase("missing").toList()

        assertNull(results.first())
    }
}

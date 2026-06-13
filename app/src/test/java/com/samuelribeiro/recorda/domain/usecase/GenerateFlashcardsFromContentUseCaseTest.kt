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

class GenerateFlashcardsFromContentUseCaseTest {

    private val repository: TopicRepository = mockk()
    private val useCase = GenerateFlashcardsFromContentUseCase(repository)
    private val topic = Topic("1", "Kotlin", emptyList())

    @Test
    fun `invoke delegates to repository`() = runTest {
        val cards = listOf(Flashcard("Q?", "A"))
        every { repository.generateFlashcards(topic) } returns flowOf(Result.success(cards))

        val results = useCase(topic).toList()

        verify(exactly = 1) { repository.generateFlashcards(topic) }
        assertEquals(cards, results.first().getOrThrow())
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val error = RuntimeException("network fail")
        every { repository.generateFlashcards(any()) } returns flowOf(Result.failure(error))

        val results = useCase(topic).toList()

        assertEquals(error, results.first().exceptionOrNull())
    }
}

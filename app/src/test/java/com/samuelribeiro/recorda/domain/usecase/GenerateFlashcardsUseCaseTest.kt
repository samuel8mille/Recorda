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

class GenerateFlashcardsUseCaseTest {

    private val repository: TopicRepository = mockk()
    private val useCase = GenerateFlashcardsUseCase(repository)

    @Test
    fun `invoke delegates to repository`() = runTest {
        val topic = Topic("1", "Kotlin", listOf(Flashcard("Q?", "A")))
        every { repository.generateFlashcards("Kotlin") } returns flowOf(Result.success(topic))

        val results = useCase("Kotlin").toList()

        verify(exactly = 1) { repository.generateFlashcards("Kotlin") }
        assertEquals(1, results.size)
        assertEquals(topic, results.first().getOrThrow())
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val error = RuntimeException("network fail")
        every { repository.generateFlashcards(any()) } returns flowOf(Result.failure(error))

        val results = useCase("Kotlin").toList()

        assertEquals(error, results.first().exceptionOrNull())
    }
}

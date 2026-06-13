package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.domain.model.MemoryCard
import com.samuelribeiro.recorda.domain.model.MemoryDeck
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContent
import com.samuelribeiro.recorda.domain.model.TopicContentStep
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class EnsureMemoryDeckUseCaseTest {

    private val ensureTopicContent: EnsureTopicContentUseCase = mockk()
    private val generateMemoryDeck: GenerateMemoryDeckUseCase = mockk()
    private val useCase = EnsureMemoryDeckUseCase(ensureTopicContent, generateMemoryDeck)

    private val content = TopicContent(listOf(Chapter("0", "Célula", "resumo", "corpo")))
    private val deck = MemoryDeck(listOf(MemoryCard("0", "Célula", "Unidade da vida")))

    @Test
    fun `returns cached deck without generating`() = runTest {
        val topic = Topic("1", "Biologia", emptyList(), memoryDeck = deck)

        val results = useCase(topic).toList()

        assertEquals(deck, results.first().getOrThrow())
        verify(exactly = 0) { ensureTopicContent(any()) }
        verify(exactly = 0) { generateMemoryDeck(any()) }
    }

    @Test
    fun `ensures content then generates deck when missing`() = runTest {
        val topic = Topic("1", "Biologia", emptyList(), content = content)
        every { ensureTopicContent(topic) } returns
            flowOf(Result.success(TopicContentStep.Completed(content)))
        every { generateMemoryDeck(any()) } returns flowOf(Result.success(deck))

        val results = useCase(topic).toList()

        assertEquals(deck, results.first().getOrThrow())
        verify { ensureTopicContent(topic) }
        verify { generateMemoryDeck(any()) }
    }

    @Test
    fun `propagates content failure and does not generate deck`() = runTest {
        val topic = Topic("1", "Biologia", emptyList())
        every { ensureTopicContent(topic) } returns flowOf(Result.failure(Exception("boom")))

        val results = useCase(topic).toList()

        assertEquals(1, results.size)
        kotlin.test.assertTrue(results.first().isFailure)
        verify(exactly = 0) { generateMemoryDeck(any()) }
    }
}

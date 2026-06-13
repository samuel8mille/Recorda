package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.MemoryCard
import com.samuelribeiro.recorda.domain.model.MemoryDeck
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.MemoryDeckRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GenerateMemoryDeckUseCaseTest {

    private val repository: MemoryDeckRepository = mockk()
    private val useCase = GenerateMemoryDeckUseCase(repository)
    private val topic = Topic("1", "Biologia", emptyList())

    @Test
    fun `invoke delegates to repository`() = runTest {
        val deck = MemoryDeck(listOf(MemoryCard("0", "Célula", "Unidade da vida")))
        every { repository.generateMemoryDeck(topic) } returns flowOf(Result.success(deck))

        val results = useCase(topic).toList()

        verify(exactly = 1) { repository.generateMemoryDeck(topic) }
        assertEquals(deck, results.first().getOrThrow())
    }
}

package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.MindMapNode
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.MindMapRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GenerateMindMapUseCaseTest {

    private val repository: MindMapRepository = mockk()
    private val useCase = GenerateMindMapUseCase(repository)
    private val topic = Topic("1", "Kotlin", emptyList())

    @Test
    fun `invoke delegates to repository`() = runTest {
        val node = MindMapNode(id = "0", title = "Kotlin", children = emptyList())
        every { repository.generateMindMap(topic) } returns flowOf(Result.success(node))

        val results = useCase(topic).toList()

        verify(exactly = 1) { repository.generateMindMap(topic) }
        assertEquals(node, results.first().getOrThrow())
    }
}

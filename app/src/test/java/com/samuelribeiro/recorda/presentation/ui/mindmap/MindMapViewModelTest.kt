package com.samuelribeiro.recorda.presentation.ui.mindmap

import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.MindMapNode
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.usecase.GenerateMindMapUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MindMapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val topicRepository: TopicRepository = mockk()
    private val getTopicUseCase = GetTopicUseCase(topicRepository)
    private val generateMindMapUseCase: GenerateMindMapUseCase = mockk()

    private val flashcards = listOf(Flashcard("O que é Kotlin?", "Uma linguagem JVM moderna"))
    private val topic = Topic(id = "topic1", name = "Kotlin", flashcards = flashcards)

    private fun createViewModel(topicId: String = "topic1"): MindMapViewModel =
        MindMapViewModel(
            topicId = topicId,
            getTopicUseCase = getTopicUseCase,
            generateMindMapUseCase = generateMindMapUseCase,
        )

    @Test
    fun `init generates mind map when topic has no cached mind map`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        val node = MindMapNode(id = "0", title = "Kotlin", children = listOf(MindMapNode(id = "0-0", title = "Sintaxe")))
        every { generateMindMapUseCase(topic) } returns flowOf(Result.success(node))

        val vm = createViewModel()

        assertEquals("Kotlin", vm.stateFlow.value.content.topicName)
        assertEquals(node, vm.stateFlow.value.content.rootNode)
        assertNull(vm.stateFlow.value.loading)
    }

    @Test
    fun `init uses cached mind map without generating a new one`() = runTest {
        val cachedNode = MindMapNode(id = "0", title = "Kotlin", children = listOf(MindMapNode(id = "0-0", title = "Sintaxe")))
        every { topicRepository.getTopic("topic1") } returns flowOf(topic.copy(mindMap = cachedNode))

        val vm = createViewModel()

        assertEquals(cachedNode, vm.stateFlow.value.content.rootNode)
        verify(exactly = 0) { generateMindMapUseCase(any()) }
    }

    @Test
    fun `ToggleNode expands and collapses a node`() = runTest {
        val cachedNode = MindMapNode(id = "0", title = "Kotlin", children = listOf(MindMapNode(id = "0-0", title = "Sintaxe")))
        every { topicRepository.getTopic("topic1") } returns flowOf(topic.copy(mindMap = cachedNode))
        val vm = createViewModel()
        assertEquals(setOf("0"), vm.stateFlow.value.content.expandedIds)

        vm.onSendEvent(ToggleNode("0-0"))
        assertEquals(setOf("0", "0-0"), vm.stateFlow.value.content.expandedIds)

        vm.onSendEvent(ToggleNode("0"))
        assertEquals(setOf("0-0"), vm.stateFlow.value.content.expandedIds)
    }

    @Test
    fun `generation failure sets error and leaves rootNode null`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        every { generateMindMapUseCase(topic) } returns flowOf(Result.failure(Exception("boom")))

        val vm = createViewModel()

        assertNull(vm.stateFlow.value.content.rootNode)
        assertNotNull(vm.stateFlow.value.error)
    }

    @Test
    fun `topic not found leaves state with default empty values`() = runTest {
        every { topicRepository.getTopic("missing") } returns flowOf(null)

        val vm = createViewModel(topicId = "missing")

        assertEquals("", vm.stateFlow.value.content.topicName)
        assertNull(vm.stateFlow.value.content.rootNode)
        assertTrue(vm.stateFlow.value.content.expandedIds.contains("0"))
    }
}

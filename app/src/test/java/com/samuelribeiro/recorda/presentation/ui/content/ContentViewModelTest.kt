package com.samuelribeiro.recorda.presentation.ui.content

import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContent
import com.samuelribeiro.recorda.domain.model.TopicContentStep
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.usecase.EnsureTopicContentUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class ContentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val topicRepository: TopicRepository = mockk()
    private val getTopicUseCase = GetTopicUseCase(topicRepository)
    private val ensureTopicContentUseCase: EnsureTopicContentUseCase = mockk()

    private val completeContent = TopicContent(
        listOf(
            Chapter("0", "Causas", "As origens", "Corpo das causas"),
            Chapter("1", "Consequências", "Os efeitos", "Corpo das consequências"),
        ),
    )
    private val topic = Topic("topic1", "Guerra", emptyList())

    private fun createViewModel(topicId: String = "topic1"): ContentViewModel =
        ContentViewModel(
            topicId = topicId,
            getTopicUseCase = getTopicUseCase,
            ensureTopicContentUseCase = ensureTopicContentUseCase,
        )

    @Test
    fun `cached complete content is shown without generating`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic.copy(content = completeContent))
        every { ensureTopicContentUseCase(any()) } returns
            flowOf(Result.success(TopicContentStep.Completed(completeContent)))

        val vm = createViewModel()

        assertEquals("Guerra", vm.stateFlow.value.content.topicName)
        assertEquals(completeContent, vm.stateFlow.value.content.content)
        assertNull(vm.stateFlow.value.content.generationProgress)
    }

    @Test
    fun `missing content triggers generation and applies steps to completion`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        every { ensureTopicContentUseCase(topic) } returns flowOf(
            Result.success(TopicContentStep.ChaptersPlanned(completeContent)),
            Result.success(TopicContentStep.ChapterGenerated(0, 2, completeContent)),
            Result.success(TopicContentStep.Completed(completeContent)),
        )

        val vm = createViewModel()

        assertEquals(completeContent, vm.stateFlow.value.content.content)
        assertNull(vm.stateFlow.value.content.generationProgress)
        verify { ensureTopicContentUseCase(topic) }
    }

    @Test
    fun `generation failure sets error`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        every { ensureTopicContentUseCase(topic) } returns flowOf(Result.failure(Exception("boom")))

        val vm = createViewModel()

        assertNotNull(vm.stateFlow.value.error)
        assertNull(vm.stateFlow.value.content.generationProgress)
    }

    @Test
    fun `SelectChapter and CloseChapter update selected chapter`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic.copy(content = completeContent))
        every { ensureTopicContentUseCase(any()) } returns
            flowOf(Result.success(TopicContentStep.Completed(completeContent)))
        val vm = createViewModel()

        vm.onSendEvent(SelectChapter("1"))
        assertEquals("1", vm.stateFlow.value.content.selectedChapterId)
        assertEquals("Consequências", vm.stateFlow.value.content.selectedChapter?.title)

        vm.onSendEvent(CloseChapter)
        assertNull(vm.stateFlow.value.content.selectedChapterId)
    }
}

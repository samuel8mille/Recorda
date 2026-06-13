package com.samuelribeiro.recorda.presentation.ui.topichub

import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TopicHubViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val topicRepository: TopicRepository = mockk()
    private val getTopicUseCase = GetTopicUseCase(topicRepository)

    private fun createViewModel(topicId: String = "topic1"): TopicHubViewModel =
        TopicHubViewModel(topicId = topicId, getTopicUseCase = getTopicUseCase)

    @Test
    fun `observes topic name into state`() = runTest {
        every { topicRepository.getTopic("topic1") } returns
            flowOf(Topic("topic1", "Kotlin", emptyList()))

        val vm = createViewModel()

        assertEquals("Kotlin", vm.stateFlow.value.content.topicName)
    }

    @Test
    fun `topic not found leaves topic name empty`() = runTest {
        every { topicRepository.getTopic("missing") } returns flowOf(null)

        val vm = createViewModel(topicId = "missing")

        assertEquals("", vm.stateFlow.value.content.topicName)
    }
}

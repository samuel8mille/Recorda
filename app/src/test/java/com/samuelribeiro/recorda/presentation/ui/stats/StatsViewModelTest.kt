package com.samuelribeiro.recorda.presentation.ui.stats

import com.samuelribeiro.recorda.domain.model.DailyReviewCount
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicStats
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.usecase.GetTopicStatsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val topicRepository: TopicRepository = mockk()
    private val getTopicUseCase = GetTopicUseCase(topicRepository)
    private val getTopicStatsUseCase: GetTopicStatsUseCase = mockk()

    private val topic = Topic(id = "topic1", name = "Kotlin", flashcards = listOf(Flashcard("Q?", "A")))

    private val stats = TopicStats(
        totalCards = 1,
        cardsOnTrack = 1,
        cardsDue = 0,
        cardsNeverReviewed = 0,
        successRate = 0.75f,
        reviewsPerDay = listOf(DailyReviewCount(date = LocalDate.of(2026, 6, 10), count = 2)),
        streakDays = 3,
        averageEaseFactor = 2.4f,
    )

    private fun createViewModel(topicId: String = "topic1"): StatsViewModel =
        StatsViewModel(
            topicId = topicId,
            getTopicUseCase = getTopicUseCase,
            getTopicStatsUseCase = getTopicStatsUseCase,
        )

    @Test
    fun `init loads topic name and stats`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        coEvery { getTopicStatsUseCase("topic1") } returns stats

        val vm = createViewModel()

        assertEquals("Kotlin", vm.stateFlow.value.content.topicName)
        assertEquals(stats, vm.stateFlow.value.content.stats)
        assertNull(vm.stateFlow.value.loading)
        assertNull(vm.stateFlow.value.error)
    }

    @Test
    fun `load failure sets error and leaves stats null`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        coEvery { getTopicStatsUseCase("topic1") } throws IllegalStateException("boom")

        val vm = createViewModel()

        assertNull(vm.stateFlow.value.content.stats)
        assertNotNull(vm.stateFlow.value.error)
    }

    @Test
    fun `RetryLoad reloads stats after failure`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        coEvery { getTopicStatsUseCase("topic1") } throws IllegalStateException("boom")
        val vm = createViewModel()
        assertNull(vm.stateFlow.value.content.stats)

        coEvery { getTopicStatsUseCase("topic1") } returns stats
        vm.onSendEvent(RetryLoad)

        assertEquals(stats, vm.stateFlow.value.content.stats)
    }

    @Test
    fun `missing topic leaves name empty but still loads stats`() = runTest {
        every { topicRepository.getTopic("missing") } returns flowOf(null)
        coEvery { getTopicStatsUseCase("missing") } returns stats

        val vm = createViewModel(topicId = "missing")

        assertEquals("", vm.stateFlow.value.content.topicName)
        assertEquals(stats, vm.stateFlow.value.content.stats)
    }
}

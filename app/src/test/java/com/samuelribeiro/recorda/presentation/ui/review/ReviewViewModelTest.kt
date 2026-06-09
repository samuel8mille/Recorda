package com.samuelribeiro.recorda.presentation.ui.review

import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.FlashcardReviewState
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.usecase.GetFlashcardReviewsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import com.samuelribeiro.recorda.domain.usecase.UpdateCardScheduleUseCase
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val topicRepository: TopicRepository = mockk()
    private val getTopicUseCase = GetTopicUseCase(topicRepository)
    private val getFlashcardReviews: GetFlashcardReviewsUseCase = mockk()
    private val updateCardSchedule: UpdateCardScheduleUseCase = mockk()

    private val flashcards = listOf(
        Flashcard("O que é Kotlin?", "Uma linguagem JVM moderna"),
        Flashcard("O que é coroutine?", "Concorrência cooperativa"),
        Flashcard("O que é Flow?", "Stream reativo de coroutines"),
    )
    private val topic = Topic(id = "topic1", name = "Kotlin", flashcards = flashcards)

    @Before
    fun setUp() {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        coEvery { getFlashcardReviews(any()) } returns emptyList()
        coEvery { updateCardSchedule(any(), any(), any()) } answers {
            val state = secondArg<FlashcardReviewState>()
            state.copy(repetitions = state.repetitions + 1)
        }
    }

    private fun createViewModel(topicId: String = "topic1"): ReviewViewModel =
        ReviewViewModel(
            topicId = topicId,
            getTopicUseCase = getTopicUseCase,
            getFlashcardReviewsUseCase = getFlashcardReviews,
            updateCardScheduleUseCase = updateCardSchedule,
        )

    @Test
    fun `init loads topic name and flashcards`() = runTest {
        val vm = createViewModel()

        assertEquals("Kotlin", vm.stateFlow.value.content.topicName)
        assertEquals(flashcards, vm.stateFlow.value.content.flashcards)
    }

    @Test
    fun `initial state has index zero and card unflipped`() = runTest {
        val vm = createViewModel()

        assertEquals(0, vm.stateFlow.value.content.currentIndex)
        assertFalse(vm.stateFlow.value.content.isFlipped)
        assertFalse(vm.stateFlow.value.content.isSessionComplete)
    }

    @Test
    fun `FlipCard toggles isFlipped to true`() = runTest {
        val vm = createViewModel()

        vm.onSendEvent(FlipCard)

        assertTrue(vm.stateFlow.value.content.isFlipped)
    }

    @Test
    fun `FlipCard twice toggles isFlipped back to false`() = runTest {
        val vm = createViewModel()

        vm.onSendEvent(FlipCard)
        vm.onSendEvent(FlipCard)

        assertFalse(vm.stateFlow.value.content.isFlipped)
    }

    @Test
    fun `RateCard AGAIN resets flip without advancing index`() = runTest {
        val vm = createViewModel()
        vm.onSendEvent(FlipCard)

        vm.onSendEvent(RateCard(CardRating.AGAIN))

        assertEquals(0, vm.stateFlow.value.content.currentIndex)
        assertFalse(vm.stateFlow.value.content.isFlipped)
    }

    @Test
    fun `RateCard GOOD advances to next card and resets flip`() = runTest {
        val vm = createViewModel()
        vm.onSendEvent(FlipCard)

        vm.onSendEvent(RateCard(CardRating.GOOD))

        assertEquals(1, vm.stateFlow.value.content.currentIndex)
        assertFalse(vm.stateFlow.value.content.isFlipped)
    }

    @Test
    fun `RateCard EASY advances to next card and resets flip`() = runTest {
        val vm = createViewModel()
        vm.onSendEvent(FlipCard)

        vm.onSendEvent(RateCard(CardRating.EASY))

        assertEquals(1, vm.stateFlow.value.content.currentIndex)
        assertFalse(vm.stateFlow.value.content.isFlipped)
    }

    @Test
    fun `RateCard GOOD on last card marks session complete`() = runTest {
        val vm = createViewModel()

        repeat(flashcards.size) {
            vm.onSendEvent(FlipCard)
            vm.onSendEvent(RateCard(CardRating.GOOD))
        }

        assertTrue(vm.stateFlow.value.content.isSessionComplete)
    }

    @Test
    fun `RateCard EASY on last card marks session complete`() = runTest {
        val vm = createViewModel()

        repeat(flashcards.size) {
            vm.onSendEvent(FlipCard)
            vm.onSendEvent(RateCard(CardRating.EASY))
        }

        assertTrue(vm.stateFlow.value.content.isSessionComplete)
    }

    @Test
    fun `topic not found leaves state with default empty values`() = runTest {
        every { topicRepository.getTopic("missing") } returns flowOf(null)
        val vm = ReviewViewModel(
            topicId = "missing",
            getTopicUseCase = getTopicUseCase,
            getFlashcardReviewsUseCase = getFlashcardReviews,
            updateCardScheduleUseCase = updateCardSchedule,
        )

        assertEquals("", vm.stateFlow.value.content.topicName)
        assertTrue(vm.stateFlow.value.content.flashcards.isEmpty())
    }

    @Test
    fun `RateCard delegates to UpdateCardScheduleUseCase`() = runTest {
        val vm = createViewModel()
        vm.onSendEvent(FlipCard)

        vm.onSendEvent(RateCard(CardRating.GOOD))

        coVerify { updateCardSchedule("topic1", any(), CardRating.GOOD) }
    }

    @Test
    fun `init loads review states via GetFlashcardReviewsUseCase`() = runTest {
        val savedState = FlashcardReviewState(cardIndex = 0, repetitions = 3)
        coEvery { getFlashcardReviews("topic1") } returns listOf(savedState)

        createViewModel()

        coVerify { getFlashcardReviews("topic1") }
    }
}

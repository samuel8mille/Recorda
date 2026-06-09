package com.samuelribeiro.recorda.presentation.ui.topic

import app.cash.turbine.test
import com.samuelribeiro.recorda.analytics.AnalyticsEvent
import com.samuelribeiro.recorda.analytics.AnalyticsTracker
import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.usecase.GenerateFlashcardsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetStoredTopicsUseCase
import com.samuelribeiro.recorda.logging.CrashReporter
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class TopicViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: TopicRepository = mockk()
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private val crashReporter: CrashReporter = mockk(relaxed = true)

    private val generateFlashcardsUseCase = GenerateFlashcardsUseCase(repository)
    private val getStoredTopicsUseCase = GetStoredTopicsUseCase(repository)

    private val topic = Topic(id = "1", name = "Kotlin", flashcards = listOf(Flashcard("Q?", "A")))

    @Before
    fun setUp() {
        every { repository.getStoredTopics() } returns flowOf(emptyList())
    }

    private fun createViewModel(existingTopics: List<Topic> = emptyList()): TopicViewModel {
        every { repository.getStoredTopics() } returns flowOf(existingTopics)
        return TopicViewModel(
            initialState = TopicUiState(),
            generateFlashcardsUseCase = generateFlashcardsUseCase,
            getStoredTopicsUseCase = getStoredTopicsUseCase,
            analyticsTracker = analyticsTracker,
            crashlyticsReporter = crashReporter,
        )
    }

    @Test
    fun `init loads stored topics into state`() = runTest {
        val stored = listOf(topic)
        val vm = createViewModel(existingTopics = stored)

        assertEquals(stored, vm.stateFlow.value.content.topics)
    }

    @Test
    fun `empty topic sets inputError and tracks EmptyTopicSubmitted`() = runTest {
        val vm = createViewModel()

        vm.onSendEvent(OnGenerateFlashcardsClick(""))

        assertNotNull(vm.stateFlow.value.content.inputError)
        verify { analyticsTracker.track(AnalyticsEvent.EmptyTopicSubmitted) }
    }

    @Test
    fun `whitespace-only topic is treated as empty`() = runTest {
        val vm = createViewModel()

        vm.onSendEvent(OnGenerateFlashcardsClick("   "))

        assertNotNull(vm.stateFlow.value.content.inputError)
        verify { analyticsTracker.track(AnalyticsEvent.EmptyTopicSubmitted) }
    }

    @Test
    fun `duplicate topic sets inputError and tracks DuplicateTopicSubmitted`() = runTest {
        val vm = createViewModel(existingTopics = listOf(topic))

        vm.onSendEvent(OnGenerateFlashcardsClick("Kotlin"))

        assertNotNull(vm.stateFlow.value.content.inputError)
        verify { analyticsTracker.track(AnalyticsEvent.DuplicateTopicSubmitted) }
    }

    @Test
    fun `duplicate check is case-insensitive`() = runTest {
        val vm = createViewModel(existingTopics = listOf(topic))

        vm.onSendEvent(OnGenerateFlashcardsClick("KOTLIN"))

        assertNotNull(vm.stateFlow.value.content.inputError)
        verify { analyticsTracker.track(AnalyticsEvent.DuplicateTopicSubmitted) }
    }

    @Test
    fun `successful generation clears error and tracks FlashcardsGenerated`() = runTest {
        every { repository.generateFlashcards("Kotlin") } returns flowOf(Result.success(topic))
        val vm = createViewModel()

        vm.onSendEvent(OnGenerateFlashcardsClick("Kotlin"))

        assertNull(vm.stateFlow.value.content.inputError)
        assertNull(vm.stateFlow.value.loading)
        verify { analyticsTracker.track(AnalyticsEvent.FlashcardsGenerated(topic.flashcards.size)) }
    }

    @Test
    fun `successful generation trims whitespace from topic name`() = runTest {
        every { repository.generateFlashcards("Kotlin") } returns flowOf(Result.success(topic))
        val vm = createViewModel()

        vm.onSendEvent(OnGenerateFlashcardsClick("  Kotlin  "))

        verify { repository.generateFlashcards("Kotlin") }
    }

    @Test
    fun `NoInternet error emits ShowError and tracks TopicQueuedOffline`() = runTest {
        val error = NetworkError.NoInternet()
        every { repository.generateFlashcards(any()) } returns flowOf(Result.failure(error))
        val vm = createViewModel()

        vm.effectFlow.test {
            vm.onSendEvent(OnGenerateFlashcardsClick("Kotlin"))
            assertIs<ShowError>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verify { analyticsTracker.track(AnalyticsEvent.TopicQueuedOffline) }
        coVerify(exactly = 0) { analyticsTracker.track(match { it is AnalyticsEvent.FlashcardsGenerationFailed }) }
    }

    @Test
    fun `Timeout error emits ShowError and tracks FlashcardsGenerationFailed`() = runTest {
        val error = NetworkError.Timeout()
        every { repository.generateFlashcards(any()) } returns flowOf(Result.failure(error))
        val vm = createViewModel()

        vm.effectFlow.test {
            vm.onSendEvent(OnGenerateFlashcardsClick("Kotlin"))
            val effect = awaitItem()
            assertIs<ShowError>(effect)
            cancelAndIgnoreRemainingEvents()
        }

        verify { analyticsTracker.track(AnalyticsEvent.FlashcardsGenerationFailed("timeout")) }
    }

    @Test
    fun `HttpError emits ShowError and tracks FlashcardsGenerationFailed with code`() = runTest {
        val error = NetworkError.HttpError(404, "Not Found")
        every { repository.generateFlashcards(any()) } returns flowOf(Result.failure(error))
        val vm = createViewModel()

        vm.effectFlow.test {
            vm.onSendEvent(OnGenerateFlashcardsClick("Kotlin"))
            assertIs<ShowError>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verify { analyticsTracker.track(AnalyticsEvent.FlashcardsGenerationFailed("http_404")) }
    }

    @Test
    fun `EmptyResponse error emits ShowError and tracks FlashcardsGenerationFailed`() = runTest {
        val error = NetworkError.EmptyResponse()
        every { repository.generateFlashcards(any()) } returns flowOf(Result.failure(error))
        val vm = createViewModel()

        vm.effectFlow.test {
            vm.onSendEvent(OnGenerateFlashcardsClick("Kotlin"))
            assertIs<ShowError>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verify { analyticsTracker.track(AnalyticsEvent.FlashcardsGenerationFailed("empty_response")) }
    }

    @Test
    fun `unknown error emits ShowError and tracks FlashcardsGenerationFailed with unknown type`() = runTest {
        val error = RuntimeException("unexpected")
        every { repository.generateFlashcards(any()) } returns flowOf(Result.failure(error))
        val vm = createViewModel()

        vm.effectFlow.test {
            vm.onSendEvent(OnGenerateFlashcardsClick("Kotlin"))
            assertIs<ShowError>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verify { analyticsTracker.track(AnalyticsEvent.FlashcardsGenerationFailed("unknown")) }
    }

    @Test
    fun `generation shows loading then hides it on completion`() = runTest {
        every { repository.generateFlashcards(any()) } returns flowOf(Result.success(topic))
        val vm = createViewModel()

        vm.onSendEvent(OnGenerateFlashcardsClick("Kotlin"))

        assertNull(vm.stateFlow.value.loading)
    }

    @Test
    fun `successful generation emits NavigateToReview effect with topic id`() = runTest {
        every { repository.generateFlashcards("Kotlin") } returns flowOf(Result.success(topic))
        val vm = createViewModel()

        vm.effectFlow.test {
            vm.onSendEvent(OnGenerateFlashcardsClick("Kotlin"))
            val effect = awaitItem()
            assertIs<NavigateToReview>(effect)
            kotlin.test.assertEquals(topic.id, effect.topicId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `valid submission clears previous inputError`() = runTest {
        every { repository.generateFlashcards("Kotlin") } returns flowOf(Result.success(topic))
        val vm = createViewModel()

        vm.onSendEvent(OnGenerateFlashcardsClick(""))
        assertNotNull(vm.stateFlow.value.content.inputError)

        vm.onSendEvent(OnGenerateFlashcardsClick("Kotlin"))
        assertNull(vm.stateFlow.value.content.inputError)
    }
}

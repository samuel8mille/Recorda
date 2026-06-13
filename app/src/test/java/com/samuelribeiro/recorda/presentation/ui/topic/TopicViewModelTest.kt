package com.samuelribeiro.recorda.presentation.ui.topic

import com.samuelribeiro.recorda.analytics.AnalyticsEvent
import com.samuelribeiro.recorda.analytics.AnalyticsTracker
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.usecase.CreateTopicUseCase
import com.samuelribeiro.recorda.domain.usecase.DeleteTopicUseCase
import com.samuelribeiro.recorda.domain.usecase.GetStoredTopicsUseCase
import com.samuelribeiro.recorda.logging.CrashReporter
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.coEvery
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class TopicViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: TopicRepository = mockk()
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private val crashReporter: CrashReporter = mockk(relaxed = true)
    private val deleteTopicUseCase: DeleteTopicUseCase = mockk()

    private val createTopicUseCase = CreateTopicUseCase(repository)
    private val getStoredTopicsUseCase = GetStoredTopicsUseCase(repository)

    private val topic = Topic(id = "1", name = "Kotlin", flashcards = listOf(Flashcard("Q?", "A")))

    @Before
    fun setUp() {
        every { repository.getStoredTopics() } returns flowOf(emptyList())
        coEvery { deleteTopicUseCase(any()) } returns Unit
        coEvery { repository.createTopic(any()) } answers { Topic("new", firstArg(), emptyList()) }
    }

    private fun createViewModel(existingTopics: List<Topic> = emptyList()): TopicViewModel {
        every { repository.getStoredTopics() } returns flowOf(existingTopics)
        return TopicViewModel(
            initialState = TopicUiState(),
            createTopicUseCase = createTopicUseCase,
            getStoredTopicsUseCase = getStoredTopicsUseCase,
            deleteTopicUseCase = deleteTopicUseCase,
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

        vm.onSendEvent(OnAddTopicClick(""))

        assertNotNull(vm.stateFlow.value.content.inputError)
        verify { analyticsTracker.track(AnalyticsEvent.EmptyTopicSubmitted) }
    }

    @Test
    fun `whitespace-only topic is treated as empty`() = runTest {
        val vm = createViewModel()

        vm.onSendEvent(OnAddTopicClick("   "))

        assertNotNull(vm.stateFlow.value.content.inputError)
        verify { analyticsTracker.track(AnalyticsEvent.EmptyTopicSubmitted) }
    }

    @Test
    fun `duplicate topic sets inputError and tracks DuplicateTopicSubmitted`() = runTest {
        val vm = createViewModel(existingTopics = listOf(topic))

        vm.onSendEvent(OnAddTopicClick("Kotlin"))

        assertNotNull(vm.stateFlow.value.content.inputError)
        verify { analyticsTracker.track(AnalyticsEvent.DuplicateTopicSubmitted) }
    }

    @Test
    fun `duplicate check is case-insensitive`() = runTest {
        val vm = createViewModel(existingTopics = listOf(topic))

        vm.onSendEvent(OnAddTopicClick("KOTLIN"))

        assertNotNull(vm.stateFlow.value.content.inputError)
        verify { analyticsTracker.track(AnalyticsEvent.DuplicateTopicSubmitted) }
    }

    @Test
    fun `valid topic creates it instantly and tracks TopicCreated`() = runTest {
        val vm = createViewModel()

        vm.onSendEvent(OnAddTopicClick("Kotlin"))

        assertNull(vm.stateFlow.value.content.inputError)
        assertNull(vm.stateFlow.value.loading)
        coVerify { repository.createTopic("Kotlin") }
        verify { analyticsTracker.track(AnalyticsEvent.TopicCreated) }
    }

    @Test
    fun `creation trims whitespace from topic name`() = runTest {
        val vm = createViewModel()

        vm.onSendEvent(OnAddTopicClick("  Kotlin  "))

        coVerify { repository.createTopic("Kotlin") }
    }

    @Test
    fun `valid submission clears previous inputError`() = runTest {
        val vm = createViewModel()

        vm.onSendEvent(OnAddTopicClick(""))
        assertNotNull(vm.stateFlow.value.content.inputError)

        vm.onSendEvent(OnAddTopicClick("Kotlin"))
        assertNull(vm.stateFlow.value.content.inputError)
    }

    @Test
    fun `RequestDeleteTopic sets pendingDeleteTopicId`() = runTest {
        val vm = createViewModel(existingTopics = listOf(topic))

        vm.onSendEvent(RequestDeleteTopic("1"))

        assertEquals("1", vm.stateFlow.value.content.pendingDeleteTopicId)
    }

    @Test
    fun `DismissDeleteDialog clears pendingDeleteTopicId`() = runTest {
        val vm = createViewModel(existingTopics = listOf(topic))
        vm.onSendEvent(RequestDeleteTopic("1"))

        vm.onSendEvent(DismissDeleteDialog)

        assertNull(vm.stateFlow.value.content.pendingDeleteTopicId)
    }

    @Test
    fun `ConfirmDeleteTopic calls DeleteTopicUseCase and clears pendingDeleteTopicId`() = runTest {
        val vm = createViewModel(existingTopics = listOf(topic))
        vm.onSendEvent(RequestDeleteTopic("1"))

        vm.onSendEvent(ConfirmDeleteTopic)

        coVerify { deleteTopicUseCase("1") }
        assertNull(vm.stateFlow.value.content.pendingDeleteTopicId)
    }

    @Test
    fun `ConfirmDeleteTopic when no pending id is a no-op`() = runTest {
        val vm = createViewModel()

        vm.onSendEvent(ConfirmDeleteTopic)

        coVerify(exactly = 0) { deleteTopicUseCase(any()) }
    }
}

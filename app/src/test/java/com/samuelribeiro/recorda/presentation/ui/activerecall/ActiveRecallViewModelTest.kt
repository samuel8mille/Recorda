package com.samuelribeiro.recorda.presentation.ui.activerecall

import com.samuelribeiro.recorda.domain.model.MemoryCard
import com.samuelribeiro.recorda.domain.model.MemoryDeck
import com.samuelribeiro.recorda.domain.model.OralAnswerEvaluation
import com.samuelribeiro.recorda.domain.model.OralAnswerVerdict
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.speech.SpeechToTextEngine
import com.samuelribeiro.recorda.domain.tts.TextToSpeechEngine
import com.samuelribeiro.recorda.domain.usecase.EnsureMemoryDeckUseCase
import com.samuelribeiro.recorda.domain.usecase.EvaluateOralAnswerUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveRecallViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val topicRepository: TopicRepository = mockk()
    private val getTopicUseCase = GetTopicUseCase(topicRepository)
    private val ensureMemoryDeckUseCase: EnsureMemoryDeckUseCase = mockk()
    private val evaluateOralAnswerUseCase: EvaluateOralAnswerUseCase = mockk()
    private val ttsEngine: TextToSpeechEngine = mockk(relaxed = true)
    private val speechToTextEngine: SpeechToTextEngine = mockk(relaxed = true)

    private val deck = MemoryDeck(
        listOf(
            MemoryCard("0", "Célula", "Unidade básica da vida"),
            MemoryCard("1", "DNA", "Molécula da hereditariedade"),
        ),
    )
    private val topic = Topic("topic1", "Biologia", emptyList())
    private val evaluation = OralAnswerEvaluation(OralAnswerVerdict.CORRECT, "Muito bem!")

    @Before
    fun setUp() {
        coEvery { speechToTextEngine.listen() } returns Result.success("resposta falada")
        every { evaluateOralAnswerUseCase(any(), any(), any()) } returns flowOf(Result.success(evaluation))
    }

    private fun createViewModel(topicId: String = "topic1"): ActiveRecallViewModel =
        ActiveRecallViewModel(
            topicId = topicId,
            getTopicUseCase = getTopicUseCase,
            ensureMemoryDeckUseCase = ensureMemoryDeckUseCase,
            evaluateOralAnswerUseCase = evaluateOralAnswerUseCase,
            ttsEngine = ttsEngine,
            speechToTextEngine = speechToTextEngine,
        )

    @Test
    fun `starts in Showing phase with the deck loaded`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        every { ensureMemoryDeckUseCase(topic) } returns flowOf(Result.success(deck))

        val vm = createViewModel()
        advanceTimeBy(1_000L)

        assertEquals("Biologia", vm.stateFlow.value.content.topicName)
        assertEquals(deck, vm.stateFlow.value.content.deck)
        assertEquals(RecallPhase.Showing, vm.stateFlow.value.content.phase)
        verify { ttsEngine.speak("Unidade básica da vida") }
    }

    @Test
    fun `after the timer it records evaluates and shows the verdict`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        every { ensureMemoryDeckUseCase(topic) } returns flowOf(Result.success(deck))
        coEvery { speechToTextEngine.listen() } returns Result.success("resposta falada")
        every { evaluateOralAnswerUseCase("Célula", "Unidade básica da vida", "resposta falada") } returns
            flowOf(Result.success(evaluation))

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(RecallPhase.Result, vm.stateFlow.value.content.phase)
        assertEquals(evaluation, vm.stateFlow.value.content.evaluation)
    }

    @Test
    fun `failed speech recognition reaches Result with no evaluation`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        every { ensureMemoryDeckUseCase(topic) } returns flowOf(Result.success(deck))
        coEvery { speechToTextEngine.listen() } returns Result.failure(Exception("no speech"))

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(RecallPhase.Result, vm.stateFlow.value.content.phase)
        assertNull(vm.stateFlow.value.content.evaluation)
    }

    @Test
    fun `NextCard advances to the next card`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        every { ensureMemoryDeckUseCase(topic) } returns flowOf(Result.success(deck))
        coEvery { speechToTextEngine.listen() } returns Result.success("resposta")
        every { evaluateOralAnswerUseCase(any(), any(), any()) } returns flowOf(Result.success(evaluation))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onSendEvent(NextCard)
        advanceTimeBy(1_000L)

        assertEquals(1, vm.stateFlow.value.content.currentIndex)
        assertEquals(RecallPhase.Showing, vm.stateFlow.value.content.phase)
    }

    @Test
    fun `NextCard on last card completes the session`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        every { ensureMemoryDeckUseCase(topic) } returns flowOf(Result.success(deck))
        coEvery { speechToTextEngine.listen() } returns Result.success("resposta")
        every { evaluateOralAnswerUseCase(any(), any(), any()) } returns flowOf(Result.success(evaluation))

        val vm = createViewModel()
        advanceUntilIdle()
        vm.onSendEvent(NextCard)
        advanceUntilIdle()

        vm.onSendEvent(NextCard)
        advanceUntilIdle()

        assertTrue(vm.stateFlow.value.content.isSessionComplete)
    }

    @Test
    fun `deck generation failure surfaces an error`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        every { ensureMemoryDeckUseCase(topic) } returns flowOf(Result.failure(Exception("boom")))

        val vm = createViewModel()
        advanceUntilIdle()

        assertNotNull(vm.stateFlow.value.error)
    }

    @Test
    fun `topic not found leaves default empty state`() = runTest {
        every { topicRepository.getTopic("missing") } returns flowOf(null)

        val vm = createViewModel(topicId = "missing")
        advanceUntilIdle()

        assertEquals("", vm.stateFlow.value.content.topicName)
        assertNull(vm.stateFlow.value.content.deck)
    }
}

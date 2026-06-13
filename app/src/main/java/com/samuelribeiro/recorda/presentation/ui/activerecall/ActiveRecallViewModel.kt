package com.samuelribeiro.recorda.presentation.ui.activerecall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ErrorUiState
import com.samuelribeiro.recorda.core.mvi.LoadingUiState
import com.samuelribeiro.recorda.core.mvi.UiEffect
import com.samuelribeiro.recorda.core.mvi.UiEffectImpl
import com.samuelribeiro.recorda.core.mvi.UiEvent
import com.samuelribeiro.recorda.core.mvi.UiEventImpl
import com.samuelribeiro.recorda.core.mvi.UiState
import com.samuelribeiro.recorda.core.mvi.UiStateImpl
import com.samuelribeiro.recorda.domain.model.MemoryCard
import com.samuelribeiro.recorda.domain.model.MemoryDeck
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.speech.SpeechToTextEngine
import com.samuelribeiro.recorda.domain.tts.TextToSpeechEngine
import com.samuelribeiro.recorda.domain.usecase.EnsureMemoryDeckUseCase
import com.samuelribeiro.recorda.domain.usecase.EvaluateOralAnswerUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * ViewModel for the active-recall session.
 *
 * Ensures the topic has an active-recall deck (generating chapter content first when missing),
 * then for each card runs the cycle: show the definition for [CARD_VISIBLE_MILLIS] (reading it
 * aloud via TTS), hide it and capture the user's spoken answer, grade it via the LLM, and show
 * the verdict. [NextCard] advances; the session ends after the last card.
 *
 * @param topicId The ID of the topic being recalled.
 * @param getTopicUseCase Observes the topic (and any cached deck) from the local DB.
 * @param ensureMemoryDeckUseCase Generates the deck (and its source content) when missing.
 * @param evaluateOralAnswerUseCase Grades the spoken answer against the card's definition.
 * @param ttsEngine Reads the definition aloud while it is visible.
 * @param speechToTextEngine Captures the user's spoken recall.
 */
@HiltViewModel(assistedFactory = ActiveRecallViewModel.ViewModelFactory::class)
class ActiveRecallViewModel @AssistedInject constructor(
    @Assisted private val topicId: String,
    private val getTopicUseCase: GetTopicUseCase,
    private val ensureMemoryDeckUseCase: EnsureMemoryDeckUseCase,
    private val evaluateOralAnswerUseCase: EvaluateOralAnswerUseCase,
    private val ttsEngine: TextToSpeechEngine,
    private val speechToTextEngine: SpeechToTextEngine,
) : ViewModel(),
    UiState<ActiveRecallUiState> by UiStateImpl(ActiveRecallUiState()),
    UiEvent<ActiveRecallUiEvent> by UiEventImpl(),
    UiEffect<ActiveRecallUiEffect> by UiEffectImpl() {

    /** Hilt assisted-inject factory for creating [ActiveRecallViewModel] with [topicId]. */
    @AssistedFactory
    fun interface ViewModelFactory {
        /** Creates an [ActiveRecallViewModel] scoped to [topicId]. */
        fun create(topicId: String): ActiveRecallViewModel
    }

    private var hasRequestedGeneration = false

    init {
        handleEvents()
        observeTopic()
    }

    private fun observeTopic() {
        viewModelScope.launch {
            getTopicUseCase(topicId).collect { topic ->
                topic ?: return@collect
                setState { copy(content = content.copy(topicName = topic.name)) }
                if (!hasRequestedGeneration) {
                    hasRequestedGeneration = true
                    prepareDeck(topic)
                }
            }
        }
    }

    private suspend fun prepareDeck(topic: Topic) {
        val deck = ensureDeck(topic) ?: return
        if (!deck.isNotEmpty) {
            showError(ErrorUiState(R.string.active_recall_error_generation))
            return
        }
        setState { copy(content = content.copy(deck = deck)) }
        runCard()
    }

    private suspend fun ensureDeck(topic: Topic): MemoryDeck? {
        var deck: MemoryDeck? = null
        ensureMemoryDeckUseCase(topic)
            .onStart { showLoading(LoadingUiState(R.string.active_recall_state_loading)) }
            .onCompletion { hideLoading() }
            .collect { result ->
                result.onSuccess { deck = it }
                    .onFailure { showError(ErrorUiState(R.string.active_recall_error_generation)) }
            }
        return deck
    }

    private suspend fun runCard() {
        val card = stateFlow.value.content.currentCard ?: return
        showDefinition(card)
        delay(CARD_VISIBLE_MILLIS)
        recordAndEvaluate(card)
    }

    private fun showDefinition(card: MemoryCard) {
        setState { copy(content = content.copy(phase = RecallPhase.Showing, evaluation = null)) }
        ttsEngine.speak(card.definition)
    }

    private suspend fun recordAndEvaluate(card: MemoryCard) {
        ttsEngine.stop()
        setState { copy(content = content.copy(phase = RecallPhase.Recording)) }
        val transcription = speechToTextEngine.listen()
        val spoken = transcription.getOrNull()
        if (spoken == null) {
            setState { copy(content = content.copy(phase = RecallPhase.Result, evaluation = null)) }
            return
        }
        setState { copy(content = content.copy(phase = RecallPhase.Evaluating)) }
        evaluateOralAnswerUseCase(card.concept, card.definition, spoken).collect { result ->
            result.onSuccess { evaluation ->
                setState { copy(content = content.copy(phase = RecallPhase.Result, evaluation = evaluation)) }
            }.onFailure {
                setState { copy(content = content.copy(phase = RecallPhase.Result, evaluation = null)) }
            }
        }
    }

    private fun handleEvents() {
        viewModelScope.launch {
            eventFlow.collect { event ->
                when (event) {
                    is NextCard -> onNextCard()
                    is RetryGeneration -> onRetryGeneration()
                }
            }
        }
    }

    private fun onNextCard() {
        val current = stateFlow.value.content
        val nextIndex = current.currentIndex + 1
        val total = current.deck?.cards?.size ?: 0
        if (nextIndex >= total) {
            ttsEngine.stop()
            setState { copy(content = content.copy(isSessionComplete = true, evaluation = null)) }
            return
        }
        setState { copy(content = content.copy(currentIndex = nextIndex, evaluation = null)) }
        viewModelScope.launch { runCard() }
    }

    private fun onRetryGeneration() {
        viewModelScope.launch {
            hideError()
            val topic = getTopicUseCase(topicId).first() ?: return@launch
            prepareDeck(topic)
        }
    }

    /** Dispatches an [ActiveRecallUiEvent] to be processed by this ViewModel. */
    fun onSendEvent(event: ActiveRecallUiEvent) {
        viewModelScope.launch { sendEvent(event) }
    }

    override fun onCleared() {
        super.onCleared()
        ttsEngine.stop()
        speechToTextEngine.cancel()
    }

    private companion object {
        const val CARD_VISIBLE_MILLIS = 8_000L
    }
}

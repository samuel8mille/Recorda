package com.samuelribeiro.recorda.presentation.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelribeiro.recorda.core.mvi.UiEffect
import com.samuelribeiro.recorda.core.mvi.UiEffectImpl
import com.samuelribeiro.recorda.core.mvi.UiEvent
import com.samuelribeiro.recorda.core.mvi.UiEventImpl
import com.samuelribeiro.recorda.core.mvi.UiState
import com.samuelribeiro.recorda.core.mvi.UiStateImpl
import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.FlashcardReviewState
import com.samuelribeiro.recorda.domain.speech.SpeechToTextEngine
import com.samuelribeiro.recorda.domain.tts.TextToSpeechEngine
import com.samuelribeiro.recorda.domain.usecase.EvaluateOralAnswerUseCase
import com.samuelribeiro.recorda.domain.usecase.GetFlashcardReviewsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import com.samuelribeiro.recorda.domain.usecase.UpdateCardScheduleUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

/**
 * ViewModel for the review session screen.
 *
 * @param topicId The ID of the topic whose flashcards will be reviewed.
 * @param getTopicUseCase Observes the topic and its flashcards from the local DB.
 * @param getFlashcardReviewsUseCase Loads saved SM-2 states for this topic's cards.
 * @param updateCardScheduleUseCase Schedules a rated card via SM-2 and persists the result.
 * @param ttsEngine Speaks card content aloud; swappable via the domain interface.
 * @param speechToTextEngine Captures the user's spoken answer; swappable via the domain interface.
 * @param evaluateOralAnswerUseCase Asks Gemini to grade the user's spoken answer.
 */
@HiltViewModel(assistedFactory = ReviewViewModel.ViewModelFactory::class)
class ReviewViewModel @AssistedInject constructor(
    @Assisted private val topicId: String,
    private val getTopicUseCase: GetTopicUseCase,
    private val getFlashcardReviewsUseCase: GetFlashcardReviewsUseCase,
    private val updateCardScheduleUseCase: UpdateCardScheduleUseCase,
    private val ttsEngine: TextToSpeechEngine,
    private val speechToTextEngine: SpeechToTextEngine,
    private val evaluateOralAnswerUseCase: EvaluateOralAnswerUseCase,
) : ViewModel(),
    UiState<ReviewUiState> by UiStateImpl(ReviewUiState()),
    UiEvent<ReviewUiEvent> by UiEventImpl(),
    UiEffect<ReviewUiEffect> by UiEffectImpl() {

    /** Hilt assisted-inject factory for creating [ReviewViewModel] with [topicId]. */
    @AssistedFactory
    interface ViewModelFactory {
        /** Creates a [ReviewViewModel] scoped to [topicId]. */
        fun create(topicId: String): ReviewViewModel
    }

    private val reviewStates = mutableMapOf<Int, FlashcardReviewState>()

    init {
        handleEvents()
        viewModelScope.launch {
            getFlashcardReviewsUseCase(topicId).forEach { state ->
                reviewStates[state.cardIndex] = state
            }
            getTopicUseCase(topicId).collect { topic ->
                topic ?: return@collect
                val due = topic.flashcards.filterIndexed { index, _ -> isDue(index) }
                val alreadyLoaded = stateFlow.value.content.flashcards.isNotEmpty()
                setState {
                    copy(
                        content = content.copy(
                            topicName = topic.name,
                            flashcards = due,
                            isNothingDue = due.isEmpty() && topic.flashcards.isNotEmpty(),
                        )
                    )
                }
                if (!alreadyLoaded && due.isNotEmpty()) ttsEngine.speak(due[0].question)
            }
        }
    }

    private fun handleEvents() {
        viewModelScope.launch {
            eventFlow.collect { event ->
                when (event) {
                    FlipCard -> onFlipCard()
                    StartOralAnswer -> onStartOralAnswer()
                    is RateCard -> onRateCard(event.rating)
                }
            }
        }
    }

    private fun isDue(cardIndex: Int): Boolean {
        val state = reviewStates[cardIndex]
        return state == null || state.nextReviewAtMillis <= System.currentTimeMillis()
    }

    private fun onFlipCard() {
        val current = stateFlow.value.content
        val flippingToAnswer = !current.isFlipped
        setState { copy(content = current.copy(isFlipped = flippingToAnswer, oralEvaluation = null)) }
        val card = current.flashcards.getOrNull(current.currentIndex) ?: return
        ttsEngine.speak(if (flippingToAnswer) card.answer else card.question)
    }

    private suspend fun onStartOralAnswer() {
        val current = stateFlow.value.content
        val card = current.flashcards.getOrNull(current.currentIndex) ?: return
        setState { copy(content = current.copy(isListening = true, oralEvaluation = null)) }
        val transcription = speechToTextEngine.listen()
        setState { copy(content = stateFlow.value.content.copy(isListening = false)) }
        transcription.onSuccess { spoken ->
            evaluateOralAnswerUseCase(card.question, card.answer, spoken).collect { result ->
                result.onSuccess { evaluation ->
                    setState {
                        copy(content = stateFlow.value.content.copy(isFlipped = true, oralEvaluation = evaluation))
                    }
                    ttsEngine.speak(card.answer)
                }
            }
        }
    }

    private suspend fun onRateCard(rating: CardRating) {
        val current = stateFlow.value.content
        val currentIndex = current.currentIndex
        val currentState = reviewStates[currentIndex] ?: FlashcardReviewState(cardIndex = currentIndex)
        val updated = updateCardScheduleUseCase(topicId, currentState, rating)
        reviewStates[currentIndex] = updated
        when (rating) {
            CardRating.AGAIN -> {
                setState { copy(content = current.copy(isFlipped = false, oralEvaluation = null)) }
                ttsEngine.speak(current.flashcards[currentIndex].question)
            }
            CardRating.GOOD, CardRating.EASY -> {
                val nextIndex = currentIndex + 1
                if (nextIndex >= current.flashcards.size) {
                    ttsEngine.stop()
                    setState { copy(content = current.copy(isSessionComplete = true, oralEvaluation = null)) }
                } else {
                    ttsEngine.speak(current.flashcards[nextIndex].question)
                    setState {
                        copy(content = current.copy(currentIndex = nextIndex, isFlipped = false, oralEvaluation = null))
                    }
                }
            }
        }
    }

    /** Dispatches a [ReviewUiEvent] to be processed by this ViewModel. */
    fun onSendEvent(event: ReviewUiEvent) {
        viewModelScope.launch { sendEvent(event) }
    }

    override fun onCleared() {
        super.onCleared()
        ttsEngine.stop()
        speechToTextEngine.cancel()
    }
}

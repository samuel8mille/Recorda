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
 */
@HiltViewModel(assistedFactory = ReviewViewModel.ViewModelFactory::class)
class ReviewViewModel @AssistedInject constructor(
    @Assisted private val topicId: String,
    private val getTopicUseCase: GetTopicUseCase,
    private val getFlashcardReviewsUseCase: GetFlashcardReviewsUseCase,
    private val updateCardScheduleUseCase: UpdateCardScheduleUseCase,
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
        observeTopic()
        handleEvents()
        loadReviewStates()
    }

    private fun observeTopic() {
        viewModelScope.launch {
            getTopicUseCase(topicId).collect { topic ->
                topic ?: return@collect
                setState {
                    copy(content = content.copy(topicName = topic.name, flashcards = topic.flashcards))
                }
            }
        }
    }

    private fun handleEvents() {
        viewModelScope.launch {
            eventFlow.collect { event ->
                when (event) {
                    FlipCard -> onFlipCard()
                    is RateCard -> onRateCard(event.rating)
                }
            }
        }
    }

    private fun loadReviewStates() {
        viewModelScope.launch {
            getFlashcardReviewsUseCase(topicId).forEach { state ->
                reviewStates[state.cardIndex] = state
            }
        }
    }

    private fun onFlipCard() {
        setState { copy(content = content.copy(isFlipped = !content.isFlipped)) }
    }

    private suspend fun onRateCard(rating: CardRating) {
        val current = stateFlow.value.content
        val currentIndex = current.currentIndex
        val currentState = reviewStates[currentIndex] ?: FlashcardReviewState(cardIndex = currentIndex)
        val updated = updateCardScheduleUseCase(topicId, currentState, rating)
        reviewStates[currentIndex] = updated
        when (rating) {
            CardRating.AGAIN -> setState { copy(content = current.copy(isFlipped = false)) }
            CardRating.GOOD, CardRating.EASY -> {
                val nextIndex = currentIndex + 1
                if (nextIndex >= current.flashcards.size) {
                    setState { copy(content = current.copy(isSessionComplete = true)) }
                } else {
                    setState {
                        copy(content = current.copy(currentIndex = nextIndex, isFlipped = false))
                    }
                }
            }
        }
    }

    /** Dispatches a [ReviewUiEvent] to be processed by this ViewModel. */
    fun onSendEvent(event: ReviewUiEvent) {
        viewModelScope.launch { sendEvent(event) }
    }
}

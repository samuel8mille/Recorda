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
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

/**
 * ViewModel for the review session screen.
 *
 * @param topicId The ID of the topic whose flashcards will be reviewed.
 * @param getTopicUseCase Use case that observes the topic and its flashcards from the local DB.
 */
@HiltViewModel(assistedFactory = ReviewViewModel.ViewModelFactory::class)
class ReviewViewModel @AssistedInject constructor(
    @Assisted private val topicId: String,
    private val getTopicUseCase: GetTopicUseCase,
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

    init {
        observeTopic()
        handleEvents()
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

    private fun onFlipCard() {
        setState { copy(content = content.copy(isFlipped = !content.isFlipped)) }
    }

    private fun onRateCard(rating: CardRating) {
        val current = stateFlow.value.content
        when (rating) {
            CardRating.AGAIN -> setState { copy(content = current.copy(isFlipped = false)) }
            CardRating.GOOD, CardRating.EASY -> {
                val nextIndex = current.currentIndex + 1
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

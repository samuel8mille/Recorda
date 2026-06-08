package com.samuelribeiro.recorda.presentation.ui.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.analytics.AnalyticsEvent
import com.samuelribeiro.recorda.analytics.AnalyticsTracker
import com.samuelribeiro.recorda.core.mvi.ErrorUiState
import com.samuelribeiro.recorda.core.mvi.LoadingUiState
import com.samuelribeiro.recorda.core.mvi.UiEffect
import com.samuelribeiro.recorda.core.mvi.UiEffectImpl
import com.samuelribeiro.recorda.core.mvi.UiEvent
import com.samuelribeiro.recorda.core.mvi.UiEventImpl
import com.samuelribeiro.recorda.core.mvi.UiState
import com.samuelribeiro.recorda.core.mvi.UiStateImpl
import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.usecase.GenerateFlashcardsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetStoredTopicsUseCase
import com.samuelribeiro.recorda.logging.CrashReporter
import com.samuelribeiro.recorda.presentation.utils.normalizeTopic
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the topic screen.
 *
 * @param initialState The initial UI state for the topic screen.
 * @param generateFlashcardsUseCase Use case that generates flashcards for a topic via the network.
 * @param getStoredTopicsUseCase Use case that observes the locally stored topic list.
 * @param analyticsTracker Tracks user interaction events.
 * @param crashlyticsReporter Reports breadcrumbs and custom keys for crash diagnostics.
 */
@HiltViewModel(assistedFactory = TopicViewModel.ViewModelFactory::class)
class TopicViewModel @AssistedInject constructor(
    @Assisted initialState: TopicUiState,
    private val generateFlashcardsUseCase: GenerateFlashcardsUseCase,
    private val getStoredTopicsUseCase: GetStoredTopicsUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashlyticsReporter: CrashReporter,
) : ViewModel(),
    UiState<TopicUiState> by UiStateImpl(initialState),
    UiEvent<TopicUiEvent> by UiEventImpl(),
    UiEffect<TopicUiEffect> by UiEffectImpl() {

    @AssistedFactory
    interface ViewModelFactory {
        fun create(initialState: TopicUiState): TopicViewModel
    }

    init {
        observeStoredTopics()
        handleEvents()
    }

    private fun observeStoredTopics() {
        viewModelScope.launch {
            getStoredTopicsUseCase().collect { topics ->
                setState { copy(content = content.copy(topics = topics)) }
                crashlyticsReporter.setStoredTopicsCount(topics.size)
            }
        }
    }

    private fun handleEvents() {
        viewModelScope.launch {
            eventFlow.collect {
                when (it) {
                    is OnGenerateFlashcardsClick -> onGenerateFlashcardsClick(it.topic)
                }
            }
        }
    }

    private fun onGenerateFlashcardsClick(topic: String) {
        crashlyticsReporter.logTopicSubmitStarted()
        validate(
            topic = topic,
            onValidationFailure = { errorMessageRes, errorType, event ->
                event?.let { analyticsTracker.track(it) }
                crashlyticsReporter.logValidationFailed(errorType)
                updateInputErrorState(ErrorUiState(errorMessageRes))
            },
            onValidationSuccess = {
                updateInputErrorState(null)
                generateFlashcards(topic)
            }
        )
    }

    private fun validate(
        topic: String,
        onValidationFailure: (Int, String, AnalyticsEvent?) -> Unit,
        onValidationSuccess: () -> Unit,
    ) {
        val normalized = normalizeTopic(topic)
        when {
            normalized.isEmpty() -> onValidationFailure(
                R.string.topic_input_error_empty,
                "empty_topic",
                AnalyticsEvent.EmptyTopicSubmitted,
            )
            stateFlow.value.content.topics.any { it.name.equals(normalized, ignoreCase = true) } ->
                onValidationFailure(
                    R.string.topic_input_error_duplicate,
                    "duplicate_topic",
                    AnalyticsEvent.DuplicateTopicSubmitted,
                )
            else -> onValidationSuccess()
        }
    }

    private fun updateInputErrorState(inputError: ErrorUiState?) {
        setState { copy(content = content.copy(inputError = inputError)) }
    }

    private fun generateFlashcards(topic: String) {
        viewModelScope.launch {
            generateFlashcardsUseCase.invoke(normalizeTopic(topic))
                .onStart { showLoading(LoadingUiState(R.string.state_loading)) }
                .onCompletion { hideLoading() }
                .collect { result ->
                    val exception = result.exceptionOrNull()
                    if (exception != null) {
                        handleFailureResult(exception)
                    } else {
                        handleSuccessResult(result.getOrThrow())
                    }
                }
        }
    }

    private fun handleSuccessResult(topic: Topic) {
        analyticsTracker.track(AnalyticsEvent.FlashcardsGenerated(topic.flashcards.size))
        crashlyticsReporter.logTopicSubmitSuccess(topic.name)
    }

    private suspend fun handleFailureResult(result: Throwable) {
        val errorType = when (result) {
            is NetworkError.Timeout -> "timeout"
            is NetworkError.NoInternet -> "no_internet"
            is NetworkError.HttpError -> "http_${result.code}"
            is NetworkError.EmptyResponse -> "empty_response"
            else -> "unknown"
        }
        if (result is NetworkError.NoInternet) {
            analyticsTracker.track(AnalyticsEvent.TopicQueuedOffline)
            crashlyticsReporter.logTopicQueuedOffline()
        } else {
            analyticsTracker.track(AnalyticsEvent.FlashcardsGenerationFailed(errorType))
        }
        crashlyticsReporter.logTopicSubmitFailed(errorType)
        crashlyticsReporter.setLastNetworkError(errorType)
        Timber.e(result, "Flashcard generation failed")
        sendEffect(ShowError(result))
    }

    /**
     * Dispatches a [TopicUiEvent] to be processed by this ViewModel.
     *
     * @param event The event triggered by the UI.
     */
    fun onSendEvent(event: TopicUiEvent) {
        viewModelScope.launch { sendEvent(event) }
    }
}

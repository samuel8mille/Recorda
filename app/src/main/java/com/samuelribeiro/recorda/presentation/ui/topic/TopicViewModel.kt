package com.samuelribeiro.recorda.presentation.ui.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.analytics.AnalyticsEvent
import com.samuelribeiro.recorda.analytics.AnalyticsTracker
import com.samuelribeiro.recorda.core.mvi.ErrorUiState
import com.samuelribeiro.recorda.core.mvi.UiEvent
import com.samuelribeiro.recorda.core.mvi.UiEventImpl
import com.samuelribeiro.recorda.core.mvi.UiState
import com.samuelribeiro.recorda.core.mvi.UiStateImpl
import com.samuelribeiro.recorda.domain.usecase.CreateTopicUseCase
import com.samuelribeiro.recorda.domain.usecase.DeleteTopicUseCase
import com.samuelribeiro.recorda.domain.usecase.GetStoredTopicsUseCase
import com.samuelribeiro.recorda.logging.CrashReporter
import com.samuelribeiro.recorda.presentation.utils.normalizeTopic
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

/**
 * ViewModel for the topic screen.
 *
 * @param initialState The initial UI state for the topic screen.
 * @param createTopicUseCase Use case that creates a topic instantly, without any network call.
 * @param getStoredTopicsUseCase Use case that observes the locally stored topic list.
 * @param deleteTopicUseCase Use case that permanently removes a topic and its review states.
 * @param analyticsTracker Tracks user interaction events.
 * @param crashlyticsReporter Reports breadcrumbs and custom keys for crash diagnostics.
 */
@HiltViewModel(assistedFactory = TopicViewModel.ViewModelFactory::class)
class TopicViewModel @AssistedInject constructor(
    @Assisted initialState: TopicUiState,
    private val createTopicUseCase: CreateTopicUseCase,
    private val getStoredTopicsUseCase: GetStoredTopicsUseCase,
    private val deleteTopicUseCase: DeleteTopicUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashlyticsReporter: CrashReporter,
) : ViewModel(),
    UiState<TopicUiState> by UiStateImpl(initialState),
    UiEvent<TopicUiEvent> by UiEventImpl() {

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
                    is OnAddTopicClick -> onAddTopicClick(it.topic)
                    is RequestDeleteTopic -> setState {
                        copy(content = content.copy(pendingDeleteTopicId = it.topicId))
                    }
                    ConfirmDeleteTopic -> onConfirmDeleteTopic()
                    DismissDeleteDialog -> setState {
                        copy(content = content.copy(pendingDeleteTopicId = null))
                    }
                }
            }
        }
    }

    private fun onConfirmDeleteTopic() {
        val topicId = stateFlow.value.content.pendingDeleteTopicId ?: return
        viewModelScope.launch {
            deleteTopicUseCase(topicId)
            setState { copy(content = content.copy(pendingDeleteTopicId = null)) }
        }
    }

    private fun onAddTopicClick(topic: String) {
        validate(
            topic = topic,
            onValidationFailure = { errorMessageRes, errorType, event ->
                event?.let { analyticsTracker.track(it) }
                crashlyticsReporter.logValidationFailed(errorType)
                updateInputErrorState(ErrorUiState(errorMessageRes))
            },
            onValidationSuccess = {
                updateInputErrorState(null)
                createTopic(normalizeTopic(topic))
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

    private fun createTopic(name: String) {
        viewModelScope.launch {
            createTopicUseCase(name)
            analyticsTracker.track(AnalyticsEvent.TopicCreated)
        }
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

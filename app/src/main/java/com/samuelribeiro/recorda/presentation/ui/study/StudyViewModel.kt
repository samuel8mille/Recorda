package com.samuelribeiro.recorda.presentation.ui.study

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
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.usecase.GenerateStudyGuideUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * ViewModel for the study guide screen.
 *
 * @param topicId The ID of the topic whose study guide is shown.
 * @param getTopicUseCase Observes the topic, including any cached study guide, from the local DB.
 * @param generateStudyGuideUseCase Asks Gemini to generate the topic's study guide.
 */
@HiltViewModel(assistedFactory = StudyViewModel.ViewModelFactory::class)
class StudyViewModel @AssistedInject constructor(
    @Assisted private val topicId: String,
    private val getTopicUseCase: GetTopicUseCase,
    private val generateStudyGuideUseCase: GenerateStudyGuideUseCase,
) : ViewModel(),
    UiState<StudyUiState> by UiStateImpl(StudyUiState()),
    UiEvent<StudyUiEvent> by UiEventImpl(),
    UiEffect<StudyUiEffect> by UiEffectImpl() {

    /** Hilt assisted-inject factory for creating [StudyViewModel] with [topicId]. */
    @AssistedFactory
    interface ViewModelFactory {
        /** Creates a [StudyViewModel] scoped to [topicId]. */
        fun create(topicId: String): StudyViewModel
    }

    private var hasRequestedGeneration = false

    init {
        handleEvents()
        viewModelScope.launch {
            getTopicUseCase(topicId).collect { topic ->
                topic ?: return@collect
                setState { copy(content = content.copy(topicName = topic.name)) }
                val cachedGuide = topic.studyGuide
                if (cachedGuide != null) {
                    setState { copy(content = content.copy(guide = cachedGuide)) }
                } else if (stateFlow.value.content.guide == null && !hasRequestedGeneration) {
                    hasRequestedGeneration = true
                    generateStudyGuide(topic)
                }
            }
        }
    }

    private fun handleEvents() {
        viewModelScope.launch {
            eventFlow.collect { event ->
                when (event) {
                    is SelectSection -> setState { copy(content = content.copy(selectedSectionId = event.sectionId)) }
                    is CloseSection -> setState { copy(content = content.copy(selectedSectionId = null)) }
                }
            }
        }
    }

    private fun generateStudyGuide(topic: Topic) {
        viewModelScope.launch {
            generateStudyGuideUseCase(topic)
                .onStart { showLoading(LoadingUiState(R.string.study_state_loading)) }
                .onCompletion { hideLoading() }
                .collect { result ->
                    result.onSuccess { guide ->
                        setState { copy(content = content.copy(guide = guide)) }
                    }.onFailure {
                        showError(ErrorUiState(R.string.study_error_generation))
                    }
                }
        }
    }

    /** Dispatches a [StudyUiEvent] to be processed by this ViewModel. */
    fun onSendEvent(event: StudyUiEvent) {
        viewModelScope.launch { sendEvent(event) }
    }
}

package com.samuelribeiro.recorda.presentation.ui.content

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
import com.samuelribeiro.recorda.domain.model.TopicContentStep
import com.samuelribeiro.recorda.domain.usecase.EnsureTopicContentUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * ViewModel for the chapter content screen.
 *
 * On open, generates the topic's chapter content if it is missing or incomplete, emitting
 * progress as each chapter body is produced. Cached content is shown immediately and, when
 * complete, no generation runs.
 *
 * @param topicId The ID of the topic whose content is shown.
 * @param getTopicUseCase Observes the topic, including any cached content, from the local DB.
 * @param ensureTopicContentUseCase Generates or resumes the topic's chapter content.
 */
@HiltViewModel(assistedFactory = ContentViewModel.ViewModelFactory::class)
class ContentViewModel @AssistedInject constructor(
    @Assisted private val topicId: String,
    private val getTopicUseCase: GetTopicUseCase,
    private val ensureTopicContentUseCase: EnsureTopicContentUseCase,
) : ViewModel(),
    UiState<ContentUiState> by UiStateImpl(ContentUiState()),
    UiEvent<ContentUiEvent> by UiEventImpl(),
    UiEffect<ContentUiEffect> by UiEffectImpl() {

    /** Hilt assisted-inject factory for creating [ContentViewModel] with [topicId]. */
    @AssistedFactory
    interface ViewModelFactory {
        /** Creates a [ContentViewModel] scoped to [topicId]. */
        fun create(topicId: String): ContentViewModel
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
                setState { copy(content = content.copy(topicName = topic.name, content = topic.content)) }
                val cached = topic.content
                if ((cached == null || !cached.isComplete) && !hasRequestedGeneration) {
                    hasRequestedGeneration = true
                    generateContent(topic)
                }
            }
        }
    }

    private fun handleEvents() {
        viewModelScope.launch {
            eventFlow.collect { event ->
                when (event) {
                    is SelectChapter -> setState { copy(content = content.copy(selectedChapterId = event.chapterId)) }
                    is CloseChapter -> setState { copy(content = content.copy(selectedChapterId = null)) }
                    is RetryGeneration -> retryGeneration()
                }
            }
        }
    }

    private fun retryGeneration() {
        viewModelScope.launch {
            hideError()
            val topic = getTopicUseCase(topicId).first() ?: return@launch
            generateContent(topic)
        }
    }

    private fun generateContent(topic: Topic) {
        viewModelScope.launch {
            ensureTopicContentUseCase(topic)
                .onStart { showLoading(LoadingUiState(R.string.content_state_loading_chapters)) }
                .onCompletion { hideLoading() }
                .collect { result ->
                    result.onSuccess { step -> applyStep(step) }
                        .onFailure {
                            setState { copy(content = content.copy(generationProgress = null)) }
                            showError(ErrorUiState(R.string.content_error_generation))
                        }
                }
        }
    }

    private fun applyStep(step: TopicContentStep) {
        when (step) {
            is TopicContentStep.ChaptersPlanned -> setState {
                copy(
                    content = content.copy(
                        content = step.content,
                        generationProgress = ChapterProgress(0, step.content.chapters.size),
                    ),
                )
            }
            is TopicContentStep.ChapterGenerated -> setState {
                copy(
                    content = content.copy(
                        content = step.content,
                        generationProgress = ChapterProgress(step.chapterIndex + 1, step.totalChapters),
                    ),
                )
            }
            is TopicContentStep.Completed -> setState {
                copy(content = content.copy(content = step.content, generationProgress = null))
            }
        }
    }

    /** Dispatches a [ContentUiEvent] to be processed by this ViewModel. */
    fun onSendEvent(event: ContentUiEvent) {
        viewModelScope.launch { sendEvent(event) }
    }
}

package com.samuelribeiro.recorda.presentation.ui.mindmap

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
import com.samuelribeiro.recorda.domain.usecase.GenerateMindMapUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * ViewModel for the mind map screen.
 *
 * @param topicId The ID of the topic whose mind map is shown.
 * @param getTopicUseCase Observes the topic, including any cached mind map, from the local DB.
 * @param generateMindMapUseCase Asks Gemini to organize the topic's flashcards into a mind map.
 */
@HiltViewModel(assistedFactory = MindMapViewModel.ViewModelFactory::class)
class MindMapViewModel @AssistedInject constructor(
    @Assisted private val topicId: String,
    private val getTopicUseCase: GetTopicUseCase,
    private val generateMindMapUseCase: GenerateMindMapUseCase,
) : ViewModel(),
    UiState<MindMapUiState> by UiStateImpl(MindMapUiState()),
    UiEvent<MindMapUiEvent> by UiEventImpl(),
    UiEffect<MindMapUiEffect> by UiEffectImpl() {

    /** Hilt assisted-inject factory for creating [MindMapViewModel] with [topicId]. */
    @AssistedFactory
    interface ViewModelFactory {
        /** Creates a [MindMapViewModel] scoped to [topicId]. */
        fun create(topicId: String): MindMapViewModel
    }

    private var hasRequestedGeneration = false

    init {
        handleEvents()
        viewModelScope.launch {
            getTopicUseCase(topicId).collect { topic ->
                topic ?: return@collect
                setState { copy(content = content.copy(topicName = topic.name)) }
                val cachedMindMap = topic.mindMap
                if (cachedMindMap != null) {
                    setState { copy(content = content.copy(rootNode = cachedMindMap)) }
                } else if (stateFlow.value.content.rootNode == null && !hasRequestedGeneration) {
                    hasRequestedGeneration = true
                    generateMindMap(topic)
                }
            }
        }
    }

    private fun handleEvents() {
        viewModelScope.launch {
            eventFlow.collect { event ->
                when (event) {
                    is ToggleNode -> onToggleNode(event.nodeId)
                }
            }
        }
    }

    private fun onToggleNode(nodeId: String) {
        val expandedIds = stateFlow.value.content.expandedIds
        val updated = if (nodeId in expandedIds) expandedIds - nodeId else expandedIds + nodeId
        setState { copy(content = content.copy(expandedIds = updated)) }
    }

    private fun generateMindMap(topic: Topic) {
        viewModelScope.launch {
            generateMindMapUseCase(topic)
                .onStart { showLoading(LoadingUiState(R.string.mind_map_state_loading)) }
                .onCompletion { hideLoading() }
                .collect { result ->
                    result.onSuccess { node ->
                        setState { copy(content = content.copy(rootNode = node)) }
                    }.onFailure {
                        showError(ErrorUiState(R.string.mind_map_error_generation))
                    }
                }
        }
    }

    /** Dispatches a [MindMapUiEvent] to be processed by this ViewModel. */
    fun onSendEvent(event: MindMapUiEvent) {
        viewModelScope.launch { sendEvent(event) }
    }
}

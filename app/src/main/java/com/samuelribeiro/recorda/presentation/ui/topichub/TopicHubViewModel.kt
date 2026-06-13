package com.samuelribeiro.recorda.presentation.ui.topichub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samuelribeiro.recorda.core.mvi.UiState
import com.samuelribeiro.recorda.core.mvi.UiStateImpl
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

/**
 * ViewModel for the topic hub screen.
 *
 * Observes only the topic name; navigation to each learning material is handled by the
 * composable, so no further state is needed here.
 *
 * @param topicId The ID of the topic whose hub is shown.
 * @param getTopicUseCase Observes the topic from the local database.
 */
@HiltViewModel(assistedFactory = TopicHubViewModel.ViewModelFactory::class)
class TopicHubViewModel @AssistedInject constructor(
    @Assisted private val topicId: String,
    private val getTopicUseCase: GetTopicUseCase,
) : ViewModel(),
    UiState<TopicHubUiState> by UiStateImpl(TopicHubUiState()) {

    /** Hilt assisted-inject factory for creating [TopicHubViewModel] with [topicId]. */
    @AssistedFactory
    interface ViewModelFactory {
        /** Creates a [TopicHubViewModel] scoped to [topicId]. */
        fun create(topicId: String): TopicHubViewModel
    }

    init {
        observeTopicName()
    }

    private fun observeTopicName() {
        viewModelScope.launch {
            getTopicUseCase(topicId).collect { topic ->
                setState { copy(content = content.copy(topicName = topic?.name.orEmpty())) }
            }
        }
    }
}

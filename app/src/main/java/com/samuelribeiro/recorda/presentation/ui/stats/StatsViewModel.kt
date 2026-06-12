package com.samuelribeiro.recorda.presentation.ui.stats

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
import com.samuelribeiro.recorda.domain.usecase.GetTopicStatsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for the retention statistics screen.
 *
 * @param topicId The ID of the topic whose statistics are shown.
 * @param getTopicUseCase Observes the topic name from the local DB.
 * @param getTopicStatsUseCase Aggregates the topic's retention statistics.
 */
@HiltViewModel(assistedFactory = StatsViewModel.ViewModelFactory::class)
class StatsViewModel @AssistedInject constructor(
    @Assisted private val topicId: String,
    private val getTopicUseCase: GetTopicUseCase,
    private val getTopicStatsUseCase: GetTopicStatsUseCase,
) : ViewModel(),
    UiState<StatsUiState> by UiStateImpl(StatsUiState()),
    UiEvent<StatsUiEvent> by UiEventImpl(),
    UiEffect<StatsUiEffect> by UiEffectImpl() {

    /** Hilt assisted-inject factory for creating [StatsViewModel] with [topicId]. */
    @AssistedFactory
    interface ViewModelFactory {
        /** Creates a [StatsViewModel] scoped to [topicId]. */
        fun create(topicId: String): StatsViewModel
    }

    init {
        handleEvents()
        loadStats()
    }

    private fun handleEvents() {
        viewModelScope.launch {
            eventFlow.collect { event ->
                when (event) {
                    is RetryLoad -> loadStats()
                }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            showLoading(LoadingUiState(R.string.stats_state_loading))
            runCatching {
                val topicName = getTopicUseCase(topicId).first()?.name.orEmpty()
                val stats = getTopicStatsUseCase(topicId)
                setState { copy(content = content.copy(topicName = topicName, stats = stats)) }
            }.onFailure { cause ->
                if (cause is CancellationException) throw cause
                showError(ErrorUiState(R.string.stats_error_loading))
            }
            hideLoading()
        }
    }

    /** Dispatches a [StatsUiEvent] to be processed by this ViewModel. */
    fun onSendEvent(event: StatsUiEvent) {
        viewModelScope.launch { sendEvent(event) }
    }
}

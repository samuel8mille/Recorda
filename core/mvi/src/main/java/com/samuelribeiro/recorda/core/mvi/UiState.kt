package com.samuelribeiro.recorda.core.mvi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Manages [ProcessUiState] for a screen, exposed as a [StateFlow]. */
interface UiState<T : ScreenUiState> {
    val stateFlow: StateFlow<ProcessUiState<T>>
    fun setState(block: ProcessUiState<T>.() -> ProcessUiState<T>)
    fun showLoading(loading: LoadingUiState)
    fun hideLoading()
    fun showError(error: ErrorUiState)
    fun hideError()
}

/** Default [UiState] implementation backed by a [MutableStateFlow]. */
class UiStateImpl<T : ScreenUiState>(value: T) : UiState<T> {
    override val stateFlow = MutableStateFlow(ProcessUiState(value))
    override fun showLoading(loading: LoadingUiState) = setState { copy(loading = loading) }
    override fun hideLoading() = setState { copy(loading = null) }
    override fun showError(error: ErrorUiState) = setState { copy(error = error) }
    override fun hideError() = setState { copy(error = null) }
    override fun setState(block: ProcessUiState<T>.() -> ProcessUiState<T>) {
        stateFlow.value = stateFlow.value.block()
    }
}

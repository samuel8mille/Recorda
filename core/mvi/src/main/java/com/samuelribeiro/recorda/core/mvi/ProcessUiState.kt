package com.samuelribeiro.recorda.core.mvi

/** Wraps [content] state with optional [loading] and [error] overlays. */
data class ProcessUiState<T : ScreenUiState>(
    val content: T,
    val loading: LoadingUiState? = null,
    val error: ErrorUiState? = null,
)

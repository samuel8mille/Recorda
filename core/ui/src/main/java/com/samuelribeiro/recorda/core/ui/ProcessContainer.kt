package com.samuelribeiro.recorda.core.ui

import androidx.compose.runtime.Composable
import com.samuelribeiro.recorda.core.mvi.ProcessUiState
import com.samuelribeiro.recorda.core.mvi.ScreenUiState

/**
 * Shows [LoadingScreen] while [uiState].loading is non-null; otherwise renders [content].
 *
 * @param uiState Current process state driving the loading/content switch.
 * @param content Composable rendered when [uiState].loading is null.
 */
@Composable
fun <T : ScreenUiState> ProcessContainer(
    uiState: ProcessUiState<T>,
    content: @Composable () -> Unit,
) {
    val loading = uiState.loading
    if (loading != null) {
        LoadingScreen(uiState = loading)
    } else {
        content()
    }
}

package com.samuelribeiro.recorda.presentation.ui.activerecall

/** User-initiated events on the active-recall session screen. */
sealed class ActiveRecallUiEvent

/** Triggered when the user advances to the next card after seeing the result. */
data object NextCard : ActiveRecallUiEvent()

/** Triggered when the user retries generation after a failure. */
data object RetryGeneration : ActiveRecallUiEvent()

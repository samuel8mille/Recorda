package com.samuelribeiro.recorda.presentation.ui.stats

/** User-initiated events on the retention statistics screen. */
sealed class StatsUiEvent

/** Triggered when the user retries loading the statistics after a failure. */
data object RetryLoad : StatsUiEvent()

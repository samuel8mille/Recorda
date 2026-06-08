package com.samuelribeiro.recorda.presentation.ui.topic

/** One-time side effects emitted by [TopicViewModel]. */
sealed class TopicUiEffect

/**
 * Signals that a non-recoverable error has occurred.
 *
 * @property error The underlying [Throwable].
 */
data class ShowError(val error: Throwable) : TopicUiEffect()

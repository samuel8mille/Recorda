package com.samuelribeiro.recorda.presentation.ui.topic

/** User-initiated events on the topic screen. */
sealed class TopicUiEvent

/**
 * Triggered when the user submits a topic to generate flashcards for.
 *
 * @property topic The raw string input from the topic text field.
 */
data class OnGenerateFlashcardsClick(val topic: String) : TopicUiEvent()

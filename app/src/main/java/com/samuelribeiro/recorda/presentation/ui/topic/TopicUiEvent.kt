package com.samuelribeiro.recorda.presentation.ui.topic

/** User-initiated events on the topic screen. */
sealed class TopicUiEvent

/**
 * Triggered when the user submits a topic to generate flashcards for.
 *
 * @property topic The raw string input from the topic text field.
 */
data class OnGenerateFlashcardsClick(val topic: String) : TopicUiEvent()

/**
 * Triggered when the user taps the delete button on a topic. Shows a confirmation dialog.
 *
 * @property topicId The ID of the topic the user wants to delete.
 */
data class RequestDeleteTopic(val topicId: String) : TopicUiEvent()

/** Triggered when the user confirms deletion in the dialog. */
data object ConfirmDeleteTopic : TopicUiEvent()

/** Triggered when the user dismisses the deletion confirmation dialog. */
data object DismissDeleteDialog : TopicUiEvent()

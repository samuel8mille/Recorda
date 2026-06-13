package com.samuelribeiro.recorda.presentation.ui.content

/** User-initiated events on the chapter content screen. */
sealed class ContentUiEvent

/**
 * Triggered when the user taps a chapter card to open its detail view.
 *
 * @property chapterId The ID of the chapter being opened.
 */
data class SelectChapter(val chapterId: String) : ContentUiEvent()

/** Triggered when the user closes the detail view to return to the chapter list. */
data object CloseChapter : ContentUiEvent()

/** Triggered when the user retries generation after a failure. */
data object RetryGeneration : ContentUiEvent()

package com.samuelribeiro.recorda.presentation.ui.study

/** User-initiated events on the study guide screen. */
sealed class StudyUiEvent

/**
 * Triggered when the user taps a section card to open its detail view.
 *
 * @property sectionId The ID of the section being opened.
 */
data class SelectSection(val sectionId: String) : StudyUiEvent()

/** Triggered when the user closes the detail view to return to the section list. */
data object CloseSection : StudyUiEvent()

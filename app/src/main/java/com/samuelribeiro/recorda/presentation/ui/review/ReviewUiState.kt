package com.samuelribeiro.recorda.presentation.ui.review

import androidx.annotation.StringRes
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ScreenUiState
import com.samuelribeiro.recorda.domain.model.Flashcard

/**
 * Content state for the review session screen.
 *
 * @property titleRes Resource ID for the screen title.
 * @property topicName The topic being reviewed.
 * @property flashcards Due cards selected for this session (already filtered by SM-2 schedule).
 * @property currentIndex Index of the card currently on screen.
 * @property isFlipped Whether the answer side is visible.
 * @property isSessionComplete Whether all cards have been rated.
 * @property isNothingDue True when the topic has flashcards but none are due today.
 */
data class ReviewUiState(
    @param:StringRes override val titleRes: Int = R.string.review_screen_title,
    val topicName: String = "",
    val flashcards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isSessionComplete: Boolean = false,
    val isNothingDue: Boolean = false,
) : ScreenUiState

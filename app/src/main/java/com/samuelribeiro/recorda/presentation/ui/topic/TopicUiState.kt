package com.samuelribeiro.recorda.presentation.ui.topic

import androidx.annotation.StringRes
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ErrorUiState
import com.samuelribeiro.recorda.core.mvi.ScreenUiState
import com.samuelribeiro.recorda.domain.model.Topic

/**
 * Represents the specific UI content state for the topic screen.
 *
 * @property titleRes The resource ID for the screen's title, defaulting to the app's name.
 * @property topics The list of [Topic]s (with their generated flashcards) to display.
 * @property inputError An optional [ErrorUiState] for the topic input field. If not null,
 *   an error message should be shown.
 * @property pendingDeleteTopicId The ID of the topic awaiting deletion confirmation, or `null`
 *   if no deletion is in progress.
 */
data class TopicUiState(
    @param:StringRes override val titleRes: Int = R.string.app_name,
    val topics: List<Topic> = emptyList(),
    val inputError: ErrorUiState? = null,
    val pendingDeleteTopicId: String? = null,
) : ScreenUiState

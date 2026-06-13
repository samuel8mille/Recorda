package com.samuelribeiro.recorda.presentation.ui.topichub

import androidx.annotation.StringRes
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ScreenUiState

/**
 * UI state for the topic hub: the grid of learning materials available for a topic.
 *
 * @property titleRes The resource ID for the screen's title.
 * @property topicName The name of the topic shown in the hub, empty until loaded.
 */
data class TopicHubUiState(
    @param:StringRes override val titleRes: Int = R.string.topic_hub_screen_title,
    val topicName: String = "",
) : ScreenUiState

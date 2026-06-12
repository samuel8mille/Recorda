package com.samuelribeiro.recorda.presentation.ui.stats

import androidx.annotation.StringRes
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ScreenUiState
import com.samuelribeiro.recorda.domain.model.TopicStats

/**
 * Content state for the retention statistics screen.
 *
 * @property titleRes Resource ID for the screen title.
 * @property topicName The topic the statistics belong to.
 * @property stats Aggregated retention statistics, or `null` while still loading.
 */
data class StatsUiState(
    @param:StringRes override val titleRes: Int = R.string.stats_screen_title,
    val topicName: String = "",
    val stats: TopicStats? = null,
) : ScreenUiState

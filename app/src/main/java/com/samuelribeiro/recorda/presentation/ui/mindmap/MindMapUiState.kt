package com.samuelribeiro.recorda.presentation.ui.mindmap

import androidx.annotation.StringRes
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ScreenUiState
import com.samuelribeiro.recorda.domain.model.MindMapNode

/**
 * Content state for the mind map screen.
 *
 * @property titleRes Resource ID for the screen title.
 * @property topicName The topic the mind map belongs to.
 * @property rootNode Root of the generated mind map tree, or `null` while it's still loading.
 * @property expandedIds IDs of nodes whose children are currently visible.
 */
data class MindMapUiState(
    @param:StringRes override val titleRes: Int = R.string.mind_map_screen_title,
    val topicName: String = "",
    val rootNode: MindMapNode? = null,
    val expandedIds: Set<String> = setOf("0"),
) : ScreenUiState

package com.samuelribeiro.recorda.presentation.ui.content

import androidx.annotation.StringRes
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ScreenUiState
import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.domain.model.TopicContent

/**
 * Progress of an in-flight chapter-body generation.
 *
 * @property current Number of chapter bodies generated so far (1-based for display).
 * @property total Total number of chapters being generated.
 */
data class ChapterProgress(
    val current: Int,
    val total: Int,
)

/**
 * Content state for the chapter content screen.
 *
 * @property titleRes Resource ID for the screen title.
 * @property topicName The topic the content belongs to.
 * @property content The (possibly partial) chapter content, or `null` while loading the list.
 * @property selectedChapterId ID of the chapter currently open in the detail view, or `null`
 * when the chapter list is showing.
 * @property generationProgress Progress of an ongoing body generation, or `null` when idle.
 */
data class ContentUiState(
    @param:StringRes override val titleRes: Int = R.string.content_screen_title,
    val topicName: String = "",
    val content: TopicContent? = null,
    val selectedChapterId: String? = null,
    val generationProgress: ChapterProgress? = null,
) : ScreenUiState {

    /** The chapter currently open in the detail view, or `null` when the list is showing. */
    val selectedChapter: Chapter?
        get() = content?.chapters?.find { it.id == selectedChapterId }
}

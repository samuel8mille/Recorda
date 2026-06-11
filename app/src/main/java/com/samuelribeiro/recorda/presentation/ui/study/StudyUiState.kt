package com.samuelribeiro.recorda.presentation.ui.study

import androidx.annotation.StringRes
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ScreenUiState
import com.samuelribeiro.recorda.domain.model.StudyGuide
import com.samuelribeiro.recorda.domain.model.StudySection

/**
 * Content state for the study guide screen.
 *
 * @property titleRes Resource ID for the screen title.
 * @property topicName The topic the study guide belongs to.
 * @property guide Generated study guide, or `null` while it's still loading.
 * @property selectedSectionId ID of the section currently open in the detail view, or `null` when
 * the section list is showing.
 */
data class StudyUiState(
    @param:StringRes override val titleRes: Int = R.string.study_screen_title,
    val topicName: String = "",
    val guide: StudyGuide? = null,
    val selectedSectionId: String? = null,
) : ScreenUiState {

    /** The section currently open in the detail view, or `null` when the list is showing. */
    val selectedSection: StudySection?
        get() = guide?.sections?.find { it.id == selectedSectionId }
}

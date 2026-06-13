package com.samuelribeiro.recorda.presentation.ui.activerecall

import androidx.annotation.StringRes
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ScreenUiState
import com.samuelribeiro.recorda.domain.model.MemoryCard
import com.samuelribeiro.recorda.domain.model.MemoryDeck
import com.samuelribeiro.recorda.domain.model.OralAnswerEvaluation

/** Phase of the active-recall flow for the current card. */
enum class RecallPhase {
    /** The definition is visible for the user to memorize. */
    Showing,

    /** The definition is hidden and the engine is capturing the spoken answer. */
    Recording,

    /** The spoken answer is being graded by the LLM. */
    Evaluating,

    /** The grading result is shown. */
    Result,
}

/**
 * Content state for the active-recall session screen.
 *
 * @property titleRes Resource ID for the screen title.
 * @property topicName The topic the deck belongs to.
 * @property deck The active-recall deck, or `null` while it is still loading.
 * @property currentIndex Index of the card currently on screen.
 * @property phase Current [RecallPhase] of the card on screen.
 * @property evaluation Grading result of the current card, or `null` until graded.
 * @property isSessionComplete Whether every card has been recalled.
 */
data class ActiveRecallUiState(
    @param:StringRes override val titleRes: Int = R.string.active_recall_screen_title,
    val topicName: String = "",
    val deck: MemoryDeck? = null,
    val currentIndex: Int = 0,
    val phase: RecallPhase = RecallPhase.Showing,
    val evaluation: OralAnswerEvaluation? = null,
    val isSessionComplete: Boolean = false,
) : ScreenUiState {

    /** The card currently on screen, or `null` when the deck is empty/loading. */
    val currentCard: MemoryCard?
        get() = deck?.cards?.getOrNull(currentIndex)
}

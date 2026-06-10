package com.samuelribeiro.recorda.presentation.ui.review

import com.samuelribeiro.recorda.domain.model.CardRating

/** User-initiated events on the review session screen. */
sealed class ReviewUiEvent

/** Triggered when the user taps the card to reveal or hide the answer. */
object FlipCard : ReviewUiEvent()

/** Triggered when the user taps the microphone button to answer the current card aloud. */
object StartOralAnswer : ReviewUiEvent()

/**
 * Triggered when the user rates how well they recalled the card.
 *
 * @property rating The self-assessed difficulty for this card.
 */
data class RateCard(val rating: CardRating) : ReviewUiEvent()

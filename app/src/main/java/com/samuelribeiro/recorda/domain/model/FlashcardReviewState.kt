package com.samuelribeiro.recorda.domain.model

/**
 * Current SM-2 scheduling state for a single flashcard within a topic.
 *
 * @property cardIndex Zero-based position of the card in the topic's flashcard list.
 * @property easeFactor SM-2 ease factor (≥ 1.3); higher means longer intervals.
 * @property intervalDays Days until the next scheduled review.
 * @property repetitions Number of consecutive correct recalls since the last reset.
 * @property nextReviewAtMillis Epoch millis when the card is due for review again.
 */
data class FlashcardReviewState(
    val cardIndex: Int,
    val easeFactor: Float = DEFAULT_EASE_FACTOR,
    val intervalDays: Int = 1,
    val repetitions: Int = 0,
    val nextReviewAtMillis: Long = 0L,
) {
    /** Default SM-2 scheduling constants for a new, never-reviewed card. */
    companion object {
        const val DEFAULT_EASE_FACTOR = 2.5f
    }
}

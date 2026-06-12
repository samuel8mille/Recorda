package com.samuelribeiro.recorda.domain.model

/**
 * A single card review event, the raw material of retention statistics.
 *
 * @property cardIndex Index of the reviewed card within its topic.
 * @property rating Self-assessment given by the user for this review.
 * @property timestampMillis Epoch millis (UTC) of when the review happened.
 */
data class ReviewLogEntry(
    val cardIndex: Int,
    val rating: CardRating,
    val timestampMillis: Long,
)

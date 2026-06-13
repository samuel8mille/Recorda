package com.samuelribeiro.recorda.domain.model

/**
 * A single active-recall card derived from a topic's chapter content.
 *
 * @property id Stable index-based identifier (e.g. "0", "1") used for ordering and selection.
 * @property concept Short label of what is being recalled, shown as the card heading and used
 * as the "question" when grading the spoken answer.
 * @property definition The text the user must memorize; shown briefly then hidden, and used as
 * the expected answer when grading.
 */
data class MemoryCard(
    val id: String,
    val concept: String,
    val definition: String,
)

/**
 * The set of active-recall cards generated for a topic.
 *
 * @property cards The cards in presentation order.
 */
data class MemoryDeck(
    val cards: List<MemoryCard>,
) {

    /** `true` when the deck has at least one card to present. */
    val isNotEmpty: Boolean
        get() = cards.isNotEmpty()
}

package com.samuelribeiro.recorda.domain.model

/**
 * AI-generated long-form reading content of a topic, organized into chapters.
 *
 * This is the primary learning material: flashcards and the mind map are derived
 * from it, so it must be complete before any derivation happens.
 *
 * @property chapters The chapters in reading order.
 */
data class TopicContent(
    val chapters: List<Chapter>,
) {

    /** `true` when every chapter has its body generated (safe to derive from). */
    val isComplete: Boolean
        get() = chapters.isNotEmpty() && chapters.all { it.body.isNotBlank() }

    /**
     * Compact representation of the whole content for derivation prompts: chapter
     * titles, summaries and the beginning of each body, bounded so the prompt never
     * grows past what Gemini handles comfortably.
     */
    fun asPromptSummary(): String = chapters.joinToString("\n") { chapter ->
        val preview = chapter.body.take(BODY_PREVIEW_CHARS)
        "Capítulo ${chapter.title}: ${chapter.summary} $preview".trim()
    }

    private companion object {
        const val BODY_PREVIEW_CHARS = 400
    }
}

/**
 * A single chapter of a topic's [TopicContent].
 *
 * @property id Stable index-based identifier (e.g. "0", "1") used for selection state.
 * @property title The chapter's name.
 * @property summary Short overview shown on the chapter card (around two sentences).
 * @property body Long-form learning text of the chapter; blank until generated.
 */
data class Chapter(
    val id: String,
    val title: String,
    val summary: String,
    val body: String = "",
)

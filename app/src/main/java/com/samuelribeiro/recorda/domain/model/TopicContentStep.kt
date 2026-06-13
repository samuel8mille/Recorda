package com.samuelribeiro.recorda.domain.model

/**
 * Progress steps emitted while a topic's content is being generated.
 */
sealed interface TopicContentStep {

    /** The current (possibly partial) content after this step. */
    val content: TopicContent

    /**
     * The chapter list was generated; bodies are still blank.
     *
     * @property content Content with chapter titles/summaries and blank bodies.
     */
    data class ChaptersPlanned(override val content: TopicContent) : TopicContentStep

    /**
     * One chapter body was generated and persisted.
     *
     * @property chapterIndex Zero-based index of the chapter just generated.
     * @property totalChapters Total number of chapters being generated.
     * @property content Content including every body generated so far.
     */
    data class ChapterGenerated(
        val chapterIndex: Int,
        val totalChapters: Int,
        override val content: TopicContent,
    ) : TopicContentStep

    /**
     * All chapter bodies are generated and cached.
     *
     * @property content The complete content.
     */
    data class Completed(override val content: TopicContent) : TopicContentStep
}

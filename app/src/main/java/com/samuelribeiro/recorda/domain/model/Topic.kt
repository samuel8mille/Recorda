package com.samuelribeiro.recorda.domain.model

/**
 * Domain model representing a study topic and the flashcards generated for it.
 *
 * @property id Stable identifier for the topic (used as the Room primary key).
 * @property name The study topic as typed by the user (e.g. "Segunda Guerra Mundial").
 * @property flashcards The set of question/answer pairs generated for this topic.
 * @property mindMap Cached mind map derived from [content], or `null` if not generated yet.
 * @property content Cached long-form chapter content, the source of flashcards, mind map and
 * the active-recall deck, or `null` if not generated yet.
 * @property memoryDeck Cached active-recall deck derived from [content], or `null` if not generated yet.
 */
data class Topic(
    val id: String,
    val name: String,
    val flashcards: List<Flashcard>,
    val mindMap: MindMapNode? = null,
    val content: TopicContent? = null,
    val memoryDeck: MemoryDeck? = null,
)

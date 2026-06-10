package com.samuelribeiro.recorda.domain.model

/**
 * Domain model representing a study topic and the flashcards generated for it.
 *
 * @property id Stable identifier for the topic (used as the Room primary key).
 * @property name The study topic as typed by the user (e.g. "Segunda Guerra Mundial").
 * @property flashcards The set of question/answer pairs generated for this topic.
 * @property mindMap Cached mind map generated from [flashcards], or `null` if not generated yet.
 */
data class Topic(
    val id: String,
    val name: String,
    val flashcards: List<Flashcard>,
    val mindMap: MindMapNode? = null,
)

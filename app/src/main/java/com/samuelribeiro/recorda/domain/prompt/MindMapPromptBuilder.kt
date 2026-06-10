package com.samuelribeiro.recorda.domain.prompt

import com.samuelribeiro.recorda.domain.model.Flashcard

/**
 * Contract for building the LLM prompt that organizes a topic's flashcards into a mind map.
 *
 * Keeping the prompt template outside the repository makes it swappable per model
 * and testable in isolation.
 */
interface MindMapPromptBuilder {

    /**
     * Returns the full prompt string to be sent to the LLM to generate a mind map.
     *
     * @param topicName The study topic the mind map is about.
     * @param flashcards The topic's flashcards, used as source material for the mind map.
     */
    fun build(topicName: String, flashcards: List<Flashcard>): String
}

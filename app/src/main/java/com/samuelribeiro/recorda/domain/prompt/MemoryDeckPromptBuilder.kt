package com.samuelribeiro.recorda.domain.prompt

/**
 * Contract for building the LLM prompt that derives an active-recall deck from a topic's content.
 *
 * Keeping the prompt template outside the repository makes it swappable per model
 * and testable in isolation.
 */
interface MemoryDeckPromptBuilder {

    /**
     * Returns the full prompt string to be sent to the LLM to generate the active-recall deck.
     *
     * @param topicName The study topic the deck is about.
     * @param contentSummary Compact summary of the topic's chapter content used as source material.
     */
    fun build(topicName: String, contentSummary: String): String
}

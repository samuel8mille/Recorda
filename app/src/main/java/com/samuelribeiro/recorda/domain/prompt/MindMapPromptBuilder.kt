package com.samuelribeiro.recorda.domain.prompt

/**
 * Contract for building the LLM prompt that organizes a topic's content into a mind map.
 *
 * Keeping the prompt template outside the repository makes it swappable per model
 * and testable in isolation.
 */
interface MindMapPromptBuilder {

    /**
     * Returns the full prompt string to be sent to the LLM to generate a mind map.
     *
     * @param topicName The study topic the mind map is about.
     * @param contentSummary Compact summary of the topic's chapter content used as source material.
     */
    fun build(topicName: String, contentSummary: String): String
}

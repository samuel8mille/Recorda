package com.samuelribeiro.recorda.domain.prompt

/**
 * Contract for building the LLM prompt that generates flashcards for a given topic.
 *
 * Keeping the prompt template outside the repository makes it swappable per model
 * and testable in isolation.
 */
interface FlashcardPromptBuilder {

    /**
     * Returns the full prompt string to be sent to the LLM for [topicName].
     *
     * @param topicName The study topic entered by the user.
     */
    fun build(topicName: String): String
}

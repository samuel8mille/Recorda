package com.samuelribeiro.recorda.domain.prompt

/**
 * Contract for building the LLM prompt that grades a spoken answer against a flashcard.
 *
 * Keeping the prompt template outside the repository makes it swappable per model
 * and testable in isolation.
 */
interface OralAnswerPromptBuilder {

    /**
     * Returns the full prompt string to be sent to the LLM for grading.
     *
     * @param question The flashcard question the user was asked.
     * @param expectedAnswer The flashcard's expected answer.
     * @param spokenAnswer The transcription of the user's spoken answer.
     */
    fun build(question: String, expectedAnswer: String, spokenAnswer: String): String
}

package com.samuelribeiro.recorda.data.prompt

import com.samuelribeiro.recorda.domain.prompt.FlashcardPromptBuilder
import javax.inject.Inject

/**
 * Gemini-specific implementation of [FlashcardPromptBuilder].
 *
 * Produces a single-line-per-card prompt in the format expected by [FlashcardMapper]:
 * `P: <pergunta> | R: <resposta>`
 */
class GeminiFlashcardPromptBuilder @Inject constructor() : FlashcardPromptBuilder {

    override fun build(topicName: String): String =
        "Gere exatamente 5 flashcards de estudo sobre o tema \"$topicName\". " +
            "Responda apenas com uma linha por flashcard, sem numeração nem texto extra, " +
            "no formato exato: P: <pergunta> | R: <resposta>"
}

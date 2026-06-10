package com.samuelribeiro.recorda.data.prompt

import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.prompt.MindMapPromptBuilder
import javax.inject.Inject

/**
 * Gemini-specific implementation of [MindMapPromptBuilder].
 *
 * Produces a one-line-per-node prompt in the indented outline format expected by
 * [com.samuelribeiro.recorda.data.mapper.MindMapMapper]: 2 spaces of indentation per level,
 * each line prefixed with `- `, with the first line (depth 0) being the topic itself.
 */
class GeminiMindMapPromptBuilder @Inject constructor() : MindMapPromptBuilder {

    override fun build(topicName: String, flashcards: List<Flashcard>): String {
        val cards = flashcards.joinToString("\n") { "P: ${it.question} | R: ${it.answer}" }
        return "A partir destes flashcards sobre \"$topicName\":\n$cards\n\n" +
            "Organize o conteúdo em um mapa mental hierárquico com até 3 níveis de profundidade. " +
            "Responda apenas com uma linha por nó, sem numeração nem texto extra, usando 2 espaços " +
            "de indentação por nível e o prefixo \"- \" em cada linha. A primeira linha (nível 0) " +
            "deve ser o título do tema \"$topicName\"."
    }
}

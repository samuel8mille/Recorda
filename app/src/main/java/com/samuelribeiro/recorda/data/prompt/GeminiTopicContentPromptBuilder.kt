package com.samuelribeiro.recorda.data.prompt

import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.domain.prompt.TopicContentPromptBuilder
import javax.inject.Inject

/**
 * Gemini prompts for the two-stage chapter content generation.
 *
 * The chapter list uses the same tolerant line-based format as the flashcards
 * pipeline ("T: ... | S: ..."), parsed by
 * [com.samuelribeiro.recorda.data.mapper.TopicContentMapper].
 */
class GeminiTopicContentPromptBuilder @Inject constructor() : TopicContentPromptBuilder {

    override fun buildChapterList(topicName: String): String =
        "Monte a estrutura de um material de estudo completo sobre \"$topicName\" em português do Brasil. " +
            "Divida o assunto em capítulos — entre 4 e 8, quantos o tema pedir. " +
            "Responda apenas com uma linha por capítulo, sem numeração nem texto extra, " +
            "exatamente no formato:\n" +
            "T: <título do capítulo> | S: <resumo do capítulo em até 2 frases>"

    override fun buildChapterBody(topicName: String, chapter: Chapter): String =
        "Escreva o conteúdo de estudo do capítulo \"${chapter.title}\" de um material sobre " +
            "\"$topicName\" em português do Brasil. Contexto do capítulo: ${chapter.summary}\n" +
            "Regras:\n" +
            "- Texto corrido em parágrafos, entre 400 e 700 palavras.\n" +
            "- Didático e aprofundado: explique conceitos, causas, exemplos e conexões.\n" +
            "- Sem títulos, sem markdown, sem listas — apenas parágrafos separados por linha em branco."
}

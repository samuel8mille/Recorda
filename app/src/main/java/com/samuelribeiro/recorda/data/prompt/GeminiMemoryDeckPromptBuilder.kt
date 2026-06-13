package com.samuelribeiro.recorda.data.prompt

import com.samuelribeiro.recorda.domain.prompt.MemoryDeckPromptBuilder
import javax.inject.Inject

/**
 * Gemini-specific implementation of [MemoryDeckPromptBuilder].
 *
 * Produces a single-line-per-card prompt in the tolerant format expected by
 * [com.samuelribeiro.recorda.data.mapper.MemoryDeckMapper]: `C: <conceito> | D: <definição>`.
 */
class GeminiMemoryDeckPromptBuilder @Inject constructor() : MemoryDeckPromptBuilder {

    override fun build(topicName: String, contentSummary: String): String =
        "A partir deste conteúdo sobre \"$topicName\":\n$contentSummary\n\n" +
            "Gere entre 6 e 10 cartões de recuperação ativa, cada um com um conceito-chave e sua " +
            "definição clara e completa, em português do Brasil. A definição deve ser autossuficiente " +
            "(1 a 3 frases), pois o usuário vai memorizá-la e depois recitá-la de memória. " +
            "Responda apenas com uma linha por cartão, sem numeração nem texto extra, " +
            "no formato exato: C: <conceito> | D: <definição>"
}

package com.samuelribeiro.recorda.data.prompt

import com.samuelribeiro.recorda.domain.prompt.OralAnswerPromptBuilder
import javax.inject.Inject

/**
 * Gemini-specific implementation of [OralAnswerPromptBuilder].
 *
 * Produces a single-line prompt in the format expected by [com.samuelribeiro.recorda.data.mapper.OralAnswerMapper]:
 * `VEREDITO: <CORRECT|PARTIAL|INCORRECT> | FEEDBACK: <feedback>`
 */
class GeminiOralAnswerPromptBuilder @Inject constructor() : OralAnswerPromptBuilder {

    override fun build(question: String, expectedAnswer: String, spokenAnswer: String): String =
        "Pergunta: \"$question\". Resposta esperada: \"$expectedAnswer\". " +
            "O usuário respondeu em voz alta: \"$spokenAnswer\". " +
            "Avalie se a resposta falada está correta, mesmo que com palavras diferentes da resposta esperada. " +
            "Responda apenas no formato exato, sem texto extra: " +
            "VEREDITO: <CORRECT|PARTIAL|INCORRECT> | FEEDBACK: <feedback breve em português, no máximo 2 frases>"
}

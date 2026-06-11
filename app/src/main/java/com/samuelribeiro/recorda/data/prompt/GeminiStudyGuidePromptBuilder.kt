package com.samuelribeiro.recorda.data.prompt

import com.samuelribeiro.recorda.domain.prompt.StudyGuidePromptBuilder
import javax.inject.Inject

/**
 * Builds the Gemini prompt that generates a structured study guide as JSON.
 *
 * The prompt pins the exact JSON schema so [com.samuelribeiro.recorda.data.mapper.StudyGuideMapper]
 * can parse the response with Gson.
 */
class GeminiStudyGuidePromptBuilder @Inject constructor() : StudyGuidePromptBuilder {

    override fun build(topicName: String): String =
        "Crie um guia de estudo sobre \"$topicName\" em português do Brasil. " +
            "Responda APENAS com JSON válido, sem cercas de markdown, sem comentários e sem texto " +
            "antes ou depois, exatamente neste formato: " +
            "{\"sections\":[{\"title\":\"...\",\"emoji\":\"...\",\"summary\":\"...\"," +
            "\"keyPoints\":[\"...\"],\"analogy\":\"...\",\"example\":\"...\",\"mnemonic\":\"...\"}]}\n" +
            "Regras:\n" +
            "- Gere de 3 a 5 sections cobrindo os principais tópicos do assunto.\n" +
            "- title: nome do tópico em até 6 palavras, preferindo o nome canônico do conceito " +
            "(será usado como termo de busca de imagem na Wikipédia).\n" +
            "- emoji: exatamente 1 emoji que represente o tópico.\n" +
            "- summary: resumo de aproximadamente 2 frases.\n" +
            "- keyPoints: lista com 3 a 5 pontos-chave, uma frase curta cada.\n" +
            "- analogy: uma analogia simples do dia a dia.\n" +
            "- example: um exemplo concreto.\n" +
            "- mnemonic: uma regra mnemônica curta para memorização."
}

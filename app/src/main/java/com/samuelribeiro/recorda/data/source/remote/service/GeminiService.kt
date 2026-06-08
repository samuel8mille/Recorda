package com.samuelribeiro.recorda.data.source.remote.service

/**
 * Network client-agnostic contract for content generation via an LLM.
 *
 * Implementations must throw [com.samuelribeiro.recorda.core.network.NetworkError] subtypes
 * on failure so that [com.samuelribeiro.recorda.core.network.ServiceExecutor] can apply
 * retry logic and error mapping regardless of the underlying HTTP client or LLM provider.
 *
 * Swap implementations (e.g. Gemini → another provider) by changing the Hilt binding —
 * no changes needed in the repository layer.
 */
interface GeminiService {

    /**
     * Sends [prompt] to the LLM and returns the raw generated text.
     *
     * @throws com.samuelribeiro.recorda.core.network.NetworkError on any failure.
     */
    suspend fun generateContent(prompt: String): String
}

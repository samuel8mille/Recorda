package com.samuelribeiro.recorda.data.source.remote.service

import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.data.source.remote.api.GeminiApi
import com.samuelribeiro.recorda.data.source.remote.dto.GenerateContentRequestDto
import javax.inject.Inject
import javax.inject.Named

/**
 * Retrofit implementation of [GeminiService].
 *
 * This is the only class in the codebase that knows about [retrofit2.Response] or the
 * Gemini request/response DTO shape. It converts HTTP-level results to the generated
 * text on success, or throws a [NetworkError] subtype on failure — keeping [GeminiService]
 * free of any HTTP client or provider-specific dependency.
 */
class RetrofitGeminiService @Inject constructor(
    private val api: GeminiApi,
    @param:Named("geminiApiKey") private val apiKey: String,
) : GeminiService {

    override suspend fun generateContent(prompt: String): String {
        val response = api.generateContent(apiKey, GenerateContentRequestDto(prompt))
        val body = when {
            response.isSuccessful -> response.body() ?: throw NetworkError.EmptyResponse()
            else -> throw NetworkError.HttpError(response.code(), response.errorBody()?.string())
        }
        val text = body.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
        return text?.takeIf { it.isNotBlank() } ?: throw NetworkError.EmptyResponse()
    }
}

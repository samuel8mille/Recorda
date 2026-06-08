package com.samuelribeiro.recorda.data.source.remote.dto

/**
 * Response body for the Gemini `generateContent` endpoint.
 *
 * Only the first candidate's first text part is used — enough for this single-prompt flow.
 */
data class GenerateContentResponseDto(
    val candidates: List<CandidateDto>,
) {
    data class CandidateDto(val content: ContentDto)

    data class ContentDto(val parts: List<PartDto>)

    data class PartDto(val text: String)
}

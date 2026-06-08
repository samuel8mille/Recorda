package com.samuelribeiro.recorda.data.source.remote.dto

/** Request body for the Gemini `generateContent` endpoint — a single user-role text part. */
data class GenerateContentRequestDto(
    val contents: List<ContentDto>,
) {
    constructor(prompt: String) : this(contents = listOf(ContentDto(parts = listOf(PartDto(prompt)))))

    data class ContentDto(val parts: List<PartDto>)

    data class PartDto(val text: String)
}

package com.samuelribeiro.recorda.data.source.remote.api

import com.samuelribeiro.recorda.data.source.remote.dto.GenerateContentRequestDto
import com.samuelribeiro.recorda.data.source.remote.dto.GenerateContentResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/** Retrofit interface for the Gemini `generateContent` REST endpoint. */
interface GeminiApi {

    @POST("v1beta/models/gemini-flash-latest:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequestDto,
    ): Response<GenerateContentResponseDto>
}

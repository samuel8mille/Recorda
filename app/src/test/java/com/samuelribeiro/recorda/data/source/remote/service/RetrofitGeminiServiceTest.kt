package com.samuelribeiro.recorda.data.source.remote.service

import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.data.source.remote.api.GeminiApi
import com.samuelribeiro.recorda.data.source.remote.dto.GenerateContentResponseDto
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RetrofitGeminiServiceTest {

    private val api: GeminiApi = mockk()
    private val service = RetrofitGeminiService(api, apiKey = "test-key")

    private fun successResponse(text: String): Response<GenerateContentResponseDto> =
        Response.success(
            GenerateContentResponseDto(
                candidates = listOf(
                    GenerateContentResponseDto.CandidateDto(
                        content = GenerateContentResponseDto.ContentDto(
                            parts = listOf(GenerateContentResponseDto.PartDto(text = text))
                        )
                    )
                )
            )
        )

    @Test
    fun `returns extracted text on successful response`() = runTest {
        coEvery { api.generateContent(any(), any()) } returns successResponse("Hello Kotlin")

        val result = service.generateContent("test prompt")

        assertEquals("Hello Kotlin", result)
    }

    @Test
    fun `throws HttpError on non-2xx response`() = runTest {
        coEvery { api.generateContent(any(), any()) } returns
            Response.error(404, "Not Found".toResponseBody())

        val ex = assertFailsWith<NetworkError.HttpError> {
            service.generateContent("test prompt")
        }
        assertEquals(404, ex.code)
    }

    @Test
    fun `throws EmptyResponse when body is null`() = runTest {
        val nullBodyResponse: Response<GenerateContentResponseDto> = mockk()
        every { nullBodyResponse.isSuccessful } returns true
        every { nullBodyResponse.body() } returns null
        coEvery { api.generateContent(any(), any()) } returns nullBodyResponse

        assertFailsWith<NetworkError.EmptyResponse> {
            service.generateContent("test prompt")
        }
    }

    @Test
    fun `throws EmptyResponse when text is blank`() = runTest {
        coEvery { api.generateContent(any(), any()) } returns successResponse("   ")

        assertFailsWith<NetworkError.EmptyResponse> {
            service.generateContent("test prompt")
        }
    }

    @Test
    fun `throws EmptyResponse when candidates list is empty`() = runTest {
        coEvery { api.generateContent(any(), any()) } returns
            Response.success(GenerateContentResponseDto(candidates = emptyList()))

        assertFailsWith<NetworkError.EmptyResponse> {
            service.generateContent("test prompt")
        }
    }
}

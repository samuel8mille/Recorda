package com.samuelribeiro.recorda.data.source.remote.service

import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.data.source.remote.api.WikipediaApi
import com.samuelribeiro.recorda.data.source.remote.dto.WikipediaImageSearchResponseDto
import com.samuelribeiro.recorda.data.source.remote.dto.WikipediaPageDto
import com.samuelribeiro.recorda.data.source.remote.dto.WikipediaQueryDto
import com.samuelribeiro.recorda.data.source.remote.dto.WikipediaThumbnailDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class RetrofitWikipediaServiceTest {

    private val api: WikipediaApi = mockk()
    private val service = RetrofitWikipediaService(api)

    @Test
    fun `returns thumbnail url of first page`() = runTest {
        coEvery { api.searchImage(any()) } returns Response.success(
            WikipediaImageSearchResponseDto(
                query = WikipediaQueryDto(
                    pages = mapOf(
                        "123" to WikipediaPageDto(thumbnail = WikipediaThumbnailDto(source = "https://img/x.jpg")),
                    ),
                ),
            ),
        )

        assertEquals("https://img/x.jpg", service.findImageUrl("Kotlin"))
    }

    @Test
    fun `returns null when page has no thumbnail`() = runTest {
        coEvery { api.searchImage(any()) } returns Response.success(
            WikipediaImageSearchResponseDto(
                query = WikipediaQueryDto(pages = mapOf("123" to WikipediaPageDto(thumbnail = null))),
            ),
        )

        assertNull(service.findImageUrl("Kotlin"))
    }

    @Test
    fun `returns null when search has no results`() = runTest {
        coEvery { api.searchImage(any()) } returns Response.success(
            WikipediaImageSearchResponseDto(query = null),
        )

        assertNull(service.findImageUrl("xyzabc"))
    }

    @Test
    fun `returns null when body is null`() = runTest {
        coEvery { api.searchImage(any()) } returns Response.success(null)

        assertNull(service.findImageUrl("Kotlin"))
    }

    @Test
    fun `throws HttpError on non-2xx response`() = runTest {
        coEvery { api.searchImage(any()) } returns Response.error(500, "boom".toResponseBody())

        val ex = assertFailsWith<NetworkError.HttpError> { service.findImageUrl("Kotlin") }
        assertEquals(500, ex.code)
    }
}

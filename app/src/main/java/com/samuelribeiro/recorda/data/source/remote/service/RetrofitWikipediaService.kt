package com.samuelribeiro.recorda.data.source.remote.service

import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.data.source.remote.api.WikipediaApi
import javax.inject.Inject

/**
 * Retrofit implementation of [WikipediaImageService].
 *
 * Mirrors [RetrofitGeminiService]: this is the only class that knows the Wikipedia
 * response shape. HTTP failures become [NetworkError.HttpError]; an empty search
 * result or a page without thumbnail yields `null` (not an error).
 */
class RetrofitWikipediaService @Inject constructor(
    private val api: WikipediaApi,
) : WikipediaImageService {

    override suspend fun findImageUrl(query: String): String? {
        val response = api.searchImage(query)
        if (!response.isSuccessful) {
            throw NetworkError.HttpError(response.code(), response.errorBody()?.string())
        }
        return response.body()?.query?.pages?.values?.firstOrNull()?.thumbnail?.source
    }
}

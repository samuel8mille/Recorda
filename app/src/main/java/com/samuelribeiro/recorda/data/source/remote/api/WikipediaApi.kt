package com.samuelribeiro.recorda.data.source.remote.api

import com.samuelribeiro.recorda.data.source.remote.dto.WikipediaImageSearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit contract for the public Wikipedia action API (no API key required).
 */
interface WikipediaApi {

    /**
     * Searches Wikipedia for [query] and returns the best match with its lead image thumbnail.
     */
    @GET(
        "w/api.php?action=query&generator=search&gsrlimit=1" +
            "&prop=pageimages&piprop=thumbnail&pithumbsize=480&format=json",
    )
    suspend fun searchImage(
        @Query("gsrsearch") query: String,
    ): Response<WikipediaImageSearchResponseDto>
}

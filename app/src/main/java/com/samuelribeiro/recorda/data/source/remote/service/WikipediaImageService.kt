package com.samuelribeiro.recorda.data.source.remote.service

/**
 * HTTP-agnostic contract for finding an illustrative image for a search term.
 *
 * Implementations throw [com.samuelribeiro.recorda.core.network.NetworkError] on transport
 * or HTTP failures; the absence of a result is not an error and yields `null`.
 */
interface WikipediaImageService {

    /**
     * Returns the thumbnail URL of the best Wikipedia match for [query], or `null` when
     * no page or no image is found.
     */
    suspend fun findImageUrl(query: String): String?
}

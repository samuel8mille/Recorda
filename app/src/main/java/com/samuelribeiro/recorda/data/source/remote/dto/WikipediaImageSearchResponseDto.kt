package com.samuelribeiro.recorda.data.source.remote.dto

/**
 * Response of the Wikipedia `action=query&generator=search&prop=pageimages` endpoint.
 *
 * @property query Search result container, absent when nothing matches.
 */
data class WikipediaImageSearchResponseDto(
    val query: WikipediaQueryDto?,
)

/**
 * Container of the pages found by the search generator.
 *
 * @property pages Map keyed by page id — the MediaWiki action API returns an object, not an array.
 */
data class WikipediaQueryDto(
    val pages: Map<String, WikipediaPageDto>?,
)

/**
 * A single Wikipedia page returned by the search.
 *
 * @property thumbnail Lead image thumbnail, absent when the page has no image.
 */
data class WikipediaPageDto(
    val thumbnail: WikipediaThumbnailDto?,
)

/**
 * Thumbnail metadata of a Wikipedia page image.
 *
 * @property source Direct URL of the thumbnail file.
 */
data class WikipediaThumbnailDto(
    val source: String?,
)

package com.samuelribeiro.recorda.domain.repository

import com.samuelribeiro.recorda.domain.model.MemoryDeck
import com.samuelribeiro.recorda.domain.model.Topic
import kotlinx.coroutines.flow.Flow

/** Contract for deriving and caching a topic's active-recall deck from its chapter content. */
fun interface MemoryDeckRepository {

    /**
     * Generates the active-recall deck of [topic] from its content and persists the result.
     *
     * @return A [Flow] emitting [Result.success] with the [MemoryDeck] on success, or
     *   [Result.failure] with a [com.samuelribeiro.recorda.core.network.NetworkError] on failure.
     */
    fun generateMemoryDeck(topic: Topic): Flow<Result<MemoryDeck>>
}

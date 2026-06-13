package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.MemoryDeck
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.MemoryDeckRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case responsible for deriving (and caching) a topic's active-recall deck from its content.
 *
 * @param repository The repository that sends the topic's content to the LLM and persists the result.
 */
class GenerateMemoryDeckUseCase(
    private val repository: MemoryDeckRepository,
) {
    /**
     * Generates the active-recall deck for [topic].
     *
     * @return A [Flow] emitting a single [Result] with either the [MemoryDeck] on success
     *   or a [com.samuelribeiro.recorda.core.network.NetworkError] on failure.
     */
    operator fun invoke(topic: Topic): Flow<Result<MemoryDeck>> = repository.generateMemoryDeck(topic)
}

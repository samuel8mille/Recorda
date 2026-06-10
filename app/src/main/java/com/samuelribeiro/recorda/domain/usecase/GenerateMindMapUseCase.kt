package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.MindMapNode
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.MindMapRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case responsible for generating (and caching) a mind map for a [Topic].
 *
 * @param repository The repository that sends the topic's flashcards to the LLM and persists the result.
 */
class GenerateMindMapUseCase(
    private val repository: MindMapRepository,
) {
    /**
     * Generates a mind map for [topic].
     *
     * @return A [Flow] emitting a single [Result] with either the [MindMapNode] tree on success
     *   or a [com.samuelribeiro.recorda.core.network.NetworkError] on failure.
     */
    operator fun invoke(topic: Topic): Flow<Result<MindMapNode>> = repository.generateMindMap(topic)
}

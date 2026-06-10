package com.samuelribeiro.recorda.domain.repository

import com.samuelribeiro.recorda.domain.model.MindMapNode
import com.samuelribeiro.recorda.domain.model.Topic
import kotlinx.coroutines.flow.Flow

/** Contract for generating and caching a mind map for a [Topic] via the LLM. */
interface MindMapRepository {

    /**
     * Sends [topic]'s flashcards to the LLM, organizes the response into a [MindMapNode] tree
     * and persists it for future visits.
     *
     * @return A [Flow] emitting [Result.success] with the [MindMapNode] tree on success, or
     *   [Result.failure] with a [com.samuelribeiro.recorda.core.network.NetworkError] on failure.
     */
    fun generateMindMap(topic: Topic): Flow<Result<MindMapNode>>
}

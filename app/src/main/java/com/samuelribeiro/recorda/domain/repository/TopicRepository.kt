package com.samuelribeiro.recorda.domain.repository

import com.samuelribeiro.recorda.domain.model.Topic
import kotlinx.coroutines.flow.Flow

/** Contract for generating and persisting study topics and their flashcards. */
interface TopicRepository {

    /** Emits the full list of stored [Topic] entries, updated whenever the database changes. */
    fun getStoredTopics(): Flow<List<Topic>>

    /**
     * Generates flashcards for [topicName] via the LLM and persists the result locally.
     *
     * @return A [Flow] emitting [Result.success] with the [Topic] on success, or
     *   [Result.failure] with a [com.samuelribeiro.recorda.core.network.NetworkError] on failure.
     */
    fun generateFlashcards(topicName: String): Flow<Result<Topic>>
}

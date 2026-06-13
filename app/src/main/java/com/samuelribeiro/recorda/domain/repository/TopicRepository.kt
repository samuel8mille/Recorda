package com.samuelribeiro.recorda.domain.repository

import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.Topic
import kotlinx.coroutines.flow.Flow

/** Contract for creating and persisting study topics and their flashcards. */
interface TopicRepository {

    /** Emits the full list of stored [Topic] entries, updated whenever the database changes. */
    fun getStoredTopics(): Flow<List<Topic>>

    /** Emits a single [Topic] by [id], or `null` if not found. Updates when the DB changes. */
    fun getTopic(id: String): Flow<Topic?>

    /** Permanently removes the topic with [id] and all its flashcards from local storage. */
    suspend fun deleteTopic(id: String)

    /**
     * Creates and persists a new topic named [name] with no flashcards yet.
     *
     * Runs entirely offline (no LLM call): flashcards and other materials are generated
     * later, on demand, from the topic's chapter content.
     *
     * @return The newly created [Topic].
     */
    suspend fun createTopic(name: String): Topic

    /**
     * Generates flashcards for [topic] from its chapter content and persists them locally.
     *
     * @return A [Flow] emitting [Result.success] with the generated [Flashcard]s on success,
     *   or [Result.failure] with a [com.samuelribeiro.recorda.core.network.NetworkError].
     */
    fun generateFlashcards(topic: Topic): Flow<Result<List<Flashcard>>>
}

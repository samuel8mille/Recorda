package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case responsible for generating flashcards for a study topic.
 *
 * @param repository The repository that provides flashcard generation.
 */
class GenerateFlashcardsUseCase(
    private val repository: TopicRepository,
) {
    /**
     * Executes flashcard generation for [topicName].
     *
     * @param topicName The study topic the user submitted.
     * @return A [Flow] emitting a single [Result] with either the [Topic] on success or a
     *   [com.samuelribeiro.recorda.core.network.NetworkError] on failure.
     */
    operator fun invoke(topicName: String): Flow<Result<Topic>> =
        repository.generateFlashcards(topicName)
}

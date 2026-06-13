package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow

/**
 * Generates flashcards for a topic from its already-generated chapter content.
 *
 * @param repository The repository that performs flashcard generation and persistence.
 */
class GenerateFlashcardsFromContentUseCase(
    private val repository: TopicRepository,
) {
    /**
     * Executes flashcard generation for [topic], deriving the cards from its content.
     *
     * @return A [Flow] emitting a single [Result] with either the generated
     *   [Flashcard]s on success or a
     *   [com.samuelribeiro.recorda.core.network.NetworkError] on failure.
     */
    operator fun invoke(topic: Topic): Flow<Result<List<Flashcard>>> =
        repository.generateFlashcards(topic)
}

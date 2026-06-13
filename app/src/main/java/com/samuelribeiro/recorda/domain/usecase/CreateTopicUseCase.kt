package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository

/**
 * Creates a new study topic instantly, with no flashcards and without any network call.
 *
 * @param repository The repository that persists the new topic.
 */
class CreateTopicUseCase(
    private val repository: TopicRepository,
) {
    /**
     * Creates and persists a topic named [name].
     *
     * @return The newly created [Topic].
     */
    suspend operator fun invoke(name: String): Topic =
        repository.createTopic(name)
}

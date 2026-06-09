package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow

/** Retrieves a single [Topic] by its [id], emitting updates whenever the database changes. */
class GetTopicUseCase(private val repository: TopicRepository) {
    /** Delegates to [TopicRepository.getTopic]. Returns `null` if the topic is not found. */
    operator fun invoke(id: String): Flow<Topic?> = repository.getTopic(id)
}

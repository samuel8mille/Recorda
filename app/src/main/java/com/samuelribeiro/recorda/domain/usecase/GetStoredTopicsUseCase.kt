package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow

/** Use case that emits the stored [Topic] list from the local database. */
class GetStoredTopicsUseCase(
    private val repository: TopicRepository,
) {
    operator fun invoke(): Flow<List<Topic>> = repository.getStoredTopics()
}

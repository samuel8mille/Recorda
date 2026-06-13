package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContentStep
import com.samuelribeiro.recorda.domain.repository.TopicContentRepository
import kotlinx.coroutines.flow.Flow

/**
 * Generates (or resumes) the chapter content of a topic, emitting each generation step.
 *
 * @param repository The repository that performs the staged generation and caching.
 */
class GenerateTopicContentUseCase(
    private val repository: TopicContentRepository,
) {
    /**
     * Starts content generation for [topic].
     *
     * @return A [Flow] of [Result]s, each carrying a [TopicContentStep] on success or a
     *   [com.samuelribeiro.recorda.core.network.NetworkError] on the first failure.
     */
    operator fun invoke(topic: Topic): Flow<Result<TopicContentStep>> =
        repository.generateTopicContent(topic)
}

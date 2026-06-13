package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContentStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Guarantees that a topic has complete chapter content before features that derive from it
 * (flashcards, mind map) run.
 *
 * If [Topic.content] is already complete, emits a single
 * [TopicContentStep.Completed] without calling the network; otherwise delegates to
 * [GenerateTopicContentUseCase], which resumes generation of any missing chapter bodies.
 *
 * @param generate The use case that performs the staged content generation.
 */
class EnsureTopicContentUseCase(
    private val generate: GenerateTopicContentUseCase,
) {
    /** Ensures [topic] has complete content, generating it if necessary. */
    operator fun invoke(topic: Topic): Flow<Result<TopicContentStep>> {
        val content = topic.content
        return if (content != null && content.isComplete) {
            flowOf(Result.success(TopicContentStep.Completed(content)))
        } else {
            generate(topic)
        }
    }
}

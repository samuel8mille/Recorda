package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.MemoryDeck
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContentStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Guarantees that a topic has an active-recall deck, generating its chapter content first when missing.
 *
 * Flow: ensures the topic's [Topic.content] is complete via [EnsureTopicContentUseCase]; once
 * complete, returns the cached [Topic.memoryDeck] if present, otherwise derives a new deck from
 * the content via [GenerateMemoryDeckUseCase]. Any failure on the content step is propagated.
 *
 * @param ensureTopicContent Ensures (or generates) the topic's chapter content.
 * @param generateMemoryDeck Derives the active-recall deck from the topic's content.
 */
class EnsureMemoryDeckUseCase(
    private val ensureTopicContent: EnsureTopicContentUseCase,
    private val generateMemoryDeck: GenerateMemoryDeckUseCase,
) {
    /** Ensures [topic] has an active-recall deck, generating content and deck as needed. */
    operator fun invoke(topic: Topic): Flow<Result<MemoryDeck>> {
        topic.memoryDeck?.takeIf { it.isNotEmpty }?.let { return flowOf(Result.success(it)) }
        return flow {
            var completedContent = topic.content
            ensureTopicContent(topic).collect { result ->
                result.onSuccess { step ->
                    if (step is TopicContentStep.Completed) completedContent = step.content
                }.onFailure {
                    emit(Result.failure(it))
                    return@collect
                }
            }
            val content = completedContent
            if (content == null || !content.isComplete) return@flow
            emitAll(generateMemoryDeck(topic.copy(content = content)))
        }
    }
}

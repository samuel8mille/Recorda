package com.samuelribeiro.recorda.domain.repository

import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContentStep
import kotlinx.coroutines.flow.Flow

/**
 * Contract for generating and caching the long-form chapter content of a topic.
 */
fun interface TopicContentRepository {

    /**
     * Generates (or resumes) the chapter content of [topic], persisting each step.
     *
     * Emits progress as [TopicContentStep]s and ends with
     * [TopicContentStep.Completed] on success or a failed [Result] on the first
     * unrecoverable error — partial progress stays cached for later resumption.
     */
    fun generateTopicContent(topic: Topic): Flow<Result<TopicContentStep>>
}

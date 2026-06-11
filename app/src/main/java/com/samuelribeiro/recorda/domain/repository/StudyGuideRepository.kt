package com.samuelribeiro.recorda.domain.repository

import com.samuelribeiro.recorda.domain.model.StudyGuide
import com.samuelribeiro.recorda.domain.model.Topic
import kotlinx.coroutines.flow.Flow

/**
 * Contract for generating and caching the AI study guide of a topic.
 */
interface StudyGuideRepository {

    /**
     * Generates a study guide for [topic] and caches it locally.
     *
     * @return A cold flow emitting the generated guide or the failure cause.
     */
    fun generateStudyGuide(topic: Topic): Flow<Result<StudyGuide>>
}

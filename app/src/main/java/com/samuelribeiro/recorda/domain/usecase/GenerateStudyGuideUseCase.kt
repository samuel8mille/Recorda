package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.StudyGuide
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.StudyGuideRepository
import kotlinx.coroutines.flow.Flow

/**
 * Generates the AI study guide for a topic.
 *
 * @param repository Source of study guide generation.
 */
class GenerateStudyGuideUseCase(
    private val repository: StudyGuideRepository,
) {

    /**
     * Generates a study guide for [topic].
     */
    operator fun invoke(topic: Topic): Flow<Result<StudyGuide>> = repository.generateStudyGuide(topic)
}

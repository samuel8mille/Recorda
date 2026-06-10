package com.samuelribeiro.recorda.domain.repository

import com.samuelribeiro.recorda.domain.model.OralAnswerEvaluation
import kotlinx.coroutines.flow.Flow

/** Contract for grading a user's spoken answer against a flashcard via the LLM. */
interface OralTestRepository {

    /**
     * Sends [question], [expectedAnswer] and [spokenAnswer] to the LLM and returns its grading.
     *
     * @return A [Flow] emitting [Result.success] with the [OralAnswerEvaluation] on success, or
     *   [Result.failure] with a [com.samuelribeiro.recorda.core.network.NetworkError] on failure.
     */
    fun evaluateAnswer(
        question: String,
        expectedAnswer: String,
        spokenAnswer: String,
    ): Flow<Result<OralAnswerEvaluation>>
}

package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.OralAnswerEvaluation
import com.samuelribeiro.recorda.domain.repository.OralTestRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case responsible for grading a user's spoken answer against a flashcard.
 *
 * @param repository The repository that sends the spoken answer to the LLM for grading.
 */
class EvaluateOralAnswerUseCase(
    private val repository: OralTestRepository,
) {
    /**
     * Executes grading of [spokenAnswer] against [expectedAnswer] for [question].
     *
     * @return A [Flow] emitting a single [Result] with either the [OralAnswerEvaluation] on
     *   success or a [com.samuelribeiro.recorda.core.network.NetworkError] on failure.
     */
    operator fun invoke(
        question: String,
        expectedAnswer: String,
        spokenAnswer: String,
    ): Flow<Result<OralAnswerEvaluation>> = repository.evaluateAnswer(question, expectedAnswer, spokenAnswer)
}

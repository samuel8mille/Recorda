package com.samuelribeiro.recorda.domain.model

/** LLM's verdict on how well a spoken answer matches the expected flashcard answer. */
enum class OralAnswerVerdict { CORRECT, PARTIAL, INCORRECT }

/**
 * Result of grading a user's spoken answer against a flashcard's expected answer.
 *
 * @property verdict How well the spoken answer matches the expected one.
 * @property feedback Short, user-facing explanation in Portuguese.
 */
data class OralAnswerEvaluation(
    val verdict: OralAnswerVerdict,
    val feedback: String,
)

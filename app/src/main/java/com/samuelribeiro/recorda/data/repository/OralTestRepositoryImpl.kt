package com.samuelribeiro.recorda.data.repository

import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.OralAnswerMapper
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.domain.model.OralAnswerEvaluation
import com.samuelribeiro.recorda.domain.prompt.OralAnswerPromptBuilder
import com.samuelribeiro.recorda.domain.repository.OralTestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [OralTestRepository] implementation that grades spoken answers via the LLM.
 *
 * Unlike [TopicRepositoryImpl], grading results are not persisted locally — each call is a
 * one-off evaluation for the current review session.
 */
class OralTestRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService,
    private val oralAnswerMapper: OralAnswerMapper,
    private val serviceExecutor: ServiceExecutor,
    private val promptBuilder: OralAnswerPromptBuilder,
) : OralTestRepository {

    override fun evaluateAnswer(
        question: String,
        expectedAnswer: String,
        spokenAnswer: String,
    ): Flow<Result<OralAnswerEvaluation>> =
        serviceExecutor.execute(isIdempotent = false) {
            geminiService.generateContent(promptBuilder.build(question, expectedAnswer, spokenAnswer))
        }.map { result -> result.map(oralAnswerMapper::toEvaluation) }
}

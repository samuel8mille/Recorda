package com.samuelribeiro.recorda.data.repository

import com.google.gson.Gson
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.StudyGuideMapper
import com.samuelribeiro.recorda.data.mapper.StudyGuideParseException
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.data.source.remote.service.WikipediaImageService
import com.samuelribeiro.recorda.domain.model.StudyGuide
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.prompt.StudyGuidePromptBuilder
import com.samuelribeiro.recorda.domain.repository.StudyGuideRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Generates the study guide via Gemini, enriches each section with a Wikipedia
 * thumbnail and caches the result in [TopicDao].
 *
 * Image lookup failures never fail the guide generation: each lookup runs through
 * its own [ServiceExecutor] flow and collapses to `null` on any failure, so the
 * section simply renders without an image.
 */
class StudyGuideRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService,
    private val studyGuideMapper: StudyGuideMapper,
    private val wikipediaImageService: WikipediaImageService,
    private val serviceExecutor: ServiceExecutor,
    private val topicDao: TopicDao,
    private val gson: Gson,
    private val promptBuilder: StudyGuidePromptBuilder,
) : StudyGuideRepository {

    override fun generateStudyGuide(topic: Topic): Flow<Result<StudyGuide>> =
        serviceExecutor.execute(isIdempotent = false) {
            geminiService.generateContent(promptBuilder.build(topic.name))
        }.map { result ->
            result.fold(
                onSuccess = { rawText -> parseEnrichAndCache(topic, rawText) },
                onFailure = { Result.failure(it) },
            )
        }

    private suspend fun parseEnrichAndCache(topic: Topic, rawText: String): Result<StudyGuide> {
        val guide = try {
            studyGuideMapper.toStudyGuide(rawText)
        } catch (e: StudyGuideParseException) {
            return Result.failure(e)
        }
        val enriched = guide.copy(
            sections = guide.sections.map { section -> section.copy(imageUrl = findImageUrl(section.title)) },
        )
        topicDao.updateStudyGuide(topic.id, gson.toJson(enriched))
        return Result.success(enriched)
    }

    private suspend fun findImageUrl(query: String): String? =
        serviceExecutor.execute(isIdempotent = true) { wikipediaImageService.findImageUrl(query) }
            .first()
            .getOrNull()
}

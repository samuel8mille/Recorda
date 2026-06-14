package com.samuelribeiro.recorda.data.repository

import com.google.gson.Gson
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.TopicContentMapper
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.data.sync.SyncCommandDispatcher
import com.samuelribeiro.recorda.data.sync.SyncCommandType
import com.samuelribeiro.recorda.data.sync.UpsertTopicContentPayload
import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContent
import com.samuelribeiro.recorda.domain.model.TopicContentStep
import com.samuelribeiro.recorda.domain.prompt.TopicContentPromptBuilder
import com.samuelribeiro.recorda.domain.repository.TopicContentRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Generates a topic's chapter content via Gemini in two stages — chapter list, then one
 * body per chapter — persisting [TopicDao.updateContent] after every step so generation
 * can resume from where it left off.
 *
 * [ServiceExecutor] emits [Result.failure] instead of throwing, so each stage checks its
 * result explicitly and returns early on failure, leaving whatever was already persisted
 * cached for the next attempt.
 */
class TopicContentRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService,
    private val topicContentMapper: TopicContentMapper,
    private val promptBuilder: TopicContentPromptBuilder,
    private val serviceExecutor: ServiceExecutor,
    private val topicDao: TopicDao,
    private val gson: Gson,
    private val syncCommandDispatcher: SyncCommandDispatcher,
) : TopicContentRepository {

    override fun generateTopicContent(topic: Topic): Flow<Result<TopicContentStep>> = flow {
        var content = topic.content ?: TopicContent(emptyList())

        if (content.chapters.isEmpty()) {
            val chapters = generateChapterList(topic.name).getOrElse {
                emit(Result.failure(it))
                return@flow
            }
            content = TopicContent(chapters)
            persist(topic.id, content)
            emit(Result.success(TopicContentStep.ChaptersPlanned(content)))
        }

        val total = content.chapters.size
        for (index in content.chapters.indices) {
            val chapter = content.chapters[index]
            if (chapter.body.isNotBlank()) continue

            val body = generateChapterBody(topic.name, chapter).getOrElse {
                emit(Result.failure(it))
                return@flow
            }
            content = content.copy(
                chapters = content.chapters.mapIndexed { i, c -> if (i == index) c.copy(body = body) else c },
            )
            persist(topic.id, content)
            emit(Result.success(TopicContentStep.ChapterGenerated(index, total, content)))
        }

        emit(Result.success(TopicContentStep.Completed(content)))
    }

    private suspend fun generateChapterList(topicName: String): Result<List<Chapter>> =
        serviceExecutor.execute(isIdempotent = false) {
            geminiService.generateContent(promptBuilder.buildChapterList(topicName))
        }.first().map(topicContentMapper::toChapters)

    private suspend fun generateChapterBody(topicName: String, chapter: Chapter): Result<String> =
        serviceExecutor.execute(isIdempotent = false) {
            geminiService.generateContent(promptBuilder.buildChapterBody(topicName, chapter))
        }.first().map(topicContentMapper::toChapterBody)

    private suspend fun persist(topicId: String, content: TopicContent) {
        val contentJson = gson.toJson(content)
        val updatedAtMillis = System.currentTimeMillis()
        topicDao.updateContent(topicId, contentJson, updatedAtMillis)
        syncCommandDispatcher.enqueue(
            SyncCommandType.UPSERT_TOPIC_CONTENT,
            topicId,
            UpsertTopicContentPayload(topicId, contentJson, updatedAtMillis),
        )
    }
}

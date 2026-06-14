package com.samuelribeiro.recorda.data.repository

import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.FlashcardMapper
import com.samuelribeiro.recorda.data.mapper.TopicEntityMapper
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.data.sync.CreateTopicPayload
import com.samuelribeiro.recorda.data.sync.DeleteTopicPayload
import com.samuelribeiro.recorda.data.sync.SyncCommandDispatcher
import com.samuelribeiro.recorda.data.sync.SyncCommandType
import com.samuelribeiro.recorda.data.sync.UpsertTopicFlashcardsPayload
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.prompt.FlashcardPromptBuilder
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

/**
 * Offline-first implementation of [TopicRepository].
 *
 * [createTopic] inserts a flashcard-less topic without touching the network — generation
 * of flashcards (and other materials) happens later, on demand, from the topic's chapter
 * content. [generateFlashcards] calls the LLM with a summary of that content and persists
 * the result on success.
 */
class TopicRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService,
    private val flashcardMapper: FlashcardMapper,
    private val topicEntityMapper: TopicEntityMapper,
    private val serviceExecutor: ServiceExecutor,
    private val topicDao: TopicDao,
    private val gson: Gson,
    private val promptBuilder: FlashcardPromptBuilder,
    private val syncCommandDispatcher: SyncCommandDispatcher,
) : TopicRepository {

    override fun getStoredTopics(): Flow<List<Topic>> =
        topicDao.getAll().map { entities -> entities.map(topicEntityMapper::toDomain) }

    override fun getTopic(id: String): Flow<Topic?> =
        topicDao.getById(id).map { it?.let(topicEntityMapper::toDomain) }

    override suspend fun deleteTopic(id: String) {
        topicDao.deleteById(id)
        syncCommandDispatcher.enqueue(SyncCommandType.DELETE_TOPIC, id, DeleteTopicPayload(id))
    }

    override suspend fun createTopic(name: String): Topic {
        val topic = Topic(
            id = UUID.randomUUID().toString(),
            name = name,
            flashcards = emptyList(),
        )
        topicDao.insert(topicEntityMapper.toEntity(topic))
        syncCommandDispatcher.enqueue(
            SyncCommandType.CREATE_TOPIC,
            topic.id,
            CreateTopicPayload(topic.id, topic.name),
        )
        return topic
    }

    override fun generateFlashcards(topic: Topic): Flow<Result<List<Flashcard>>> {
        val contentSummary = topic.content?.asPromptSummary().orEmpty()
        return serviceExecutor.execute(isIdempotent = false) {
            geminiService.generateContent(promptBuilder.build(topic.name, contentSummary))
        }.map { result ->
            result.map { rawText ->
                val flashcards = flashcardMapper.toFlashcards(rawText)
                val flashcardsJson = gson.toJson(flashcards)
                val updatedAtMillis = System.currentTimeMillis()
                topicDao.updateFlashcards(topic.id, flashcardsJson, updatedAtMillis)
                syncCommandDispatcher.enqueue(
                    SyncCommandType.UPSERT_TOPIC_FLASHCARDS,
                    topic.id,
                    UpsertTopicFlashcardsPayload(topic.id, flashcardsJson, updatedAtMillis),
                )
                flashcards
            }
        }
    }
}

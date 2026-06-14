package com.samuelribeiro.recorda.data.repository

import com.google.gson.Gson
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.MemoryDeckMapper
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.data.sync.SyncCommandDispatcher
import com.samuelribeiro.recorda.data.sync.SyncCommandType
import com.samuelribeiro.recorda.data.sync.UpsertTopicMemoryCardsPayload
import com.samuelribeiro.recorda.domain.model.MemoryDeck
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.prompt.MemoryDeckPromptBuilder
import com.samuelribeiro.recorda.domain.repository.MemoryDeckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [MemoryDeckRepository] implementation that derives an active-recall deck via the LLM and caches it.
 *
 * On success, the resulting [MemoryDeck] is persisted as JSON on the topic's row via
 * [TopicDao.updateMemoryCards], so future sessions reuse it without calling the LLM again.
 */
class MemoryDeckRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService,
    private val memoryDeckMapper: MemoryDeckMapper,
    private val serviceExecutor: ServiceExecutor,
    private val topicDao: TopicDao,
    private val gson: Gson,
    private val promptBuilder: MemoryDeckPromptBuilder,
    private val syncCommandDispatcher: SyncCommandDispatcher,
) : MemoryDeckRepository {

    override fun generateMemoryDeck(topic: Topic): Flow<Result<MemoryDeck>> =
        serviceExecutor.execute(isIdempotent = false) {
            geminiService.generateContent(promptBuilder.build(topic.name, topic.content?.asPromptSummary().orEmpty()))
        }.map { result ->
            result.map { rawText ->
                val deck = memoryDeckMapper.toMemoryDeck(rawText)
                val memoryCardsJson = gson.toJson(deck)
                val updatedAtMillis = System.currentTimeMillis()
                topicDao.updateMemoryCards(topic.id, memoryCardsJson, updatedAtMillis)
                syncCommandDispatcher.enqueue(
                    SyncCommandType.UPSERT_TOPIC_MEMORY_CARDS,
                    topic.id,
                    UpsertTopicMemoryCardsPayload(topic.id, memoryCardsJson, updatedAtMillis),
                )
                deck
            }
        }
}

package com.samuelribeiro.recorda.data.repository

import com.google.gson.Gson
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.MindMapMapper
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.data.sync.SyncCommandDispatcher
import com.samuelribeiro.recorda.data.sync.SyncCommandType
import com.samuelribeiro.recorda.data.sync.UpsertTopicMindMapPayload
import com.samuelribeiro.recorda.domain.model.MindMapNode
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.prompt.MindMapPromptBuilder
import com.samuelribeiro.recorda.domain.repository.MindMapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [MindMapRepository] implementation that generates a mind map via the LLM and caches it.
 *
 * On success, the resulting [MindMapNode] tree is persisted as JSON on the topic's row via
 * [TopicDao.updateMindMap], so future visits can reuse it without calling the LLM again.
 */
class MindMapRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService,
    private val mindMapMapper: MindMapMapper,
    private val serviceExecutor: ServiceExecutor,
    private val topicDao: TopicDao,
    private val gson: Gson,
    private val promptBuilder: MindMapPromptBuilder,
    private val syncCommandDispatcher: SyncCommandDispatcher,
) : MindMapRepository {

    override fun generateMindMap(topic: Topic): Flow<Result<MindMapNode>> =
        serviceExecutor.execute(isIdempotent = false) {
            geminiService.generateContent(promptBuilder.build(topic.name, topic.content?.asPromptSummary().orEmpty()))
        }.map { result ->
            result.map { rawText ->
                val node = mindMapMapper.toMindMap(topic.name, rawText)
                val mindMapJson = gson.toJson(node)
                val updatedAtMillis = System.currentTimeMillis()
                topicDao.updateMindMap(topic.id, mindMapJson, updatedAtMillis)
                syncCommandDispatcher.enqueue(
                    SyncCommandType.UPSERT_TOPIC_MIND_MAP,
                    topic.id,
                    UpsertTopicMindMapPayload(topic.id, mindMapJson, updatedAtMillis),
                )
                node
            }
        }
}

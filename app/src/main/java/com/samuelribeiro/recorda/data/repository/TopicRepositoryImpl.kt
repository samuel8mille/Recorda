package com.samuelribeiro.recorda.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.FlashcardMapper
import com.samuelribeiro.recorda.data.mapper.TopicEntityMapper
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.local.TopicEntity
import com.samuelribeiro.recorda.data.source.local.TopicStatus
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.logging.CrashReporter
import com.samuelribeiro.recorda.work.GenerateContentWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val BACKOFF_DELAY_SECONDS = 30L

/**
 * Offline-first implementation of [TopicRepository].
 *
 * [getStoredTopics] always reads from the local database. [generateFlashcards] calls the
 * LLM and persists the result to the database on success. When offline
 * ([NetworkError.NoInternet]), saves a [TopicStatus.PENDING] record and enqueues
 * [GenerateContentWorker] to sync when connectivity is restored.
 */
class TopicRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService,
    private val flashcardMapper: FlashcardMapper,
    private val topicEntityMapper: TopicEntityMapper,
    private val serviceExecutor: ServiceExecutor,
    private val topicDao: TopicDao,
    private val workManager: WorkManager,
    private val crashlyticsReporter: CrashReporter,
) : TopicRepository {

    override fun getStoredTopics(): Flow<List<Topic>> =
        topicDao.getAll().map { entities -> entities.map(topicEntityMapper::toDomain) }

    override fun getTopic(id: String): Flow<Topic?> =
        topicDao.getById(id).map { it?.let(topicEntityMapper::toDomain) }

    override fun generateFlashcards(topicName: String): Flow<Result<Topic>> =
        serviceExecutor.execute(isIdempotent = false) {
            geminiService.generateContent(buildPrompt(topicName))
        }.map { result ->
            result.fold(
                onSuccess = { rawText ->
                    val topic = Topic(
                        id = UUID.randomUUID().toString(),
                        name = topicName,
                        flashcards = flashcardMapper.toFlashcards(rawText),
                    )
                    topicDao.insert(topicEntityMapper.toEntity(topic))
                    Result.success(topic)
                },
                onFailure = { error ->
                    if (error is NetworkError.NoInternet || error is NetworkError.Timeout) {
                        enqueuePending(topicName)
                    }
                    Result.failure(error)
                },
            )
        }

    private suspend fun enqueuePending(topicName: String) {
        crashlyticsReporter.logTopicQueuedOffline()
        val pendingId = "pending_${System.currentTimeMillis()}"
        topicDao.insert(
            TopicEntity(
                id = pendingId,
                name = topicName,
                flashcardsJson = "",
                status = TopicStatus.PENDING,
            ),
        )
        val request = OneTimeWorkRequestBuilder<GenerateContentWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY_SECONDS, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            GenerateContentWorker.WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request,
        )
    }

    private fun buildPrompt(topicName: String): String =
        "Gere exatamente 5 flashcards de estudo sobre o tema \"$topicName\". " +
            "Responda apenas com uma linha por flashcard, sem numeração nem texto extra, " +
            "no formato exato: P: <pergunta> | R: <resposta>"
}

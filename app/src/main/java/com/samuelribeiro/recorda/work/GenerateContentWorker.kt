package com.samuelribeiro.recorda.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.samuelribeiro.recorda.data.mapper.FlashcardMapper
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.logging.CrashReporter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Processes topics that were queued as [com.samuelribeiro.recorda.data.source.local.TopicStatus.PENDING]
 * while the device was offline.
 *
 * Triggered automatically by WorkManager when a network connection becomes available.
 * Retries with exponential backoff on failure (configured at enqueue time).
 */
@HiltWorker
class GenerateContentWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val geminiService: GeminiService,
    private val flashcardMapper: FlashcardMapper,
    private val gson: Gson,
    private val dao: TopicDao,
    private val crashlyticsReporter: CrashReporter,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pending = dao.getPending()
        if (pending.isEmpty()) return Result.success()

        crashlyticsReporter.logWorkerSyncStarted(pending.size)
        crashlyticsReporter.setPendingTopicsCount(pending.size)
        Timber.d("Processing ${pending.size} pending topic(s)")

        var allSucceeded = true
        pending.forEach { entity ->
            runCatching {
                val rawText = geminiService.generateContent(buildPrompt(entity.name))
                val flashcardsJson = gson.toJson(flashcardMapper.toFlashcards(rawText))
                dao.markDone(id = entity.id, flashcardsJson = flashcardsJson)
                Timber.d("Synced pending topic: ${entity.name}")
            }.onFailure {
                Timber.e(it, "Failed to sync pending topic: ${entity.name}")
                allSucceeded = false
            }
        }

        return if (allSucceeded) {
            crashlyticsReporter.logWorkerSyncSuccess()
            crashlyticsReporter.setPendingTopicsCount(0)
            Result.success()
        } else {
            crashlyticsReporter.logWorkerSyncFailed()
            Result.retry()
        }
    }

    private fun buildPrompt(topicName: String): String =
        "Gere exatamente 5 flashcards de estudo sobre o tema \"$topicName\". " +
            "Responda apenas com uma linha por flashcard, sem numeração nem texto extra, " +
            "no formato exato: P: <pergunta> | R: <resposta>"

    companion object {
        const val WORK_NAME = "generate_content_sync"
    }
}

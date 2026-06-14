package com.samuelribeiro.recorda.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Thin WorkManager wrapper that drains the local sync queue via [SyncEngine] and maps its
 * [SyncEngine.Outcome] to a WorkManager [Result] (retry triggers exponential backoff).
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncEngine: SyncEngine,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = when (syncEngine.drain(runAttemptCount)) {
        SyncEngine.Outcome.SUCCESS -> Result.success()
        SyncEngine.Outcome.RETRY -> Result.retry()
        SyncEngine.Outcome.FAILURE -> Result.failure()
    }

    /** Unique work names used by the scheduler to coalesce sync requests. */
    companion object {
        const val WORK_NAME_ONESHOT = "sync-oneshot"
        const val WORK_NAME_PERIODIC = "sync-periodic"
    }
}

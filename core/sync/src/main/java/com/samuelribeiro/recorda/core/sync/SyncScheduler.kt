package com.samuelribeiro.recorda.core.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Schedules [SyncWorker] both opportunistically (after each local mutation) and periodically
 * (a ~15 min safety net), each gated on network connectivity.
 *
 * [WorkManager] is injected lazily via [Provider] so the instance is resolved at use time
 * (after WorkManager is initialized), and so the scheduler is unit-testable with a fake.
 */
@Singleton
class SyncScheduler @Inject constructor(
    private val workManagerProvider: Provider<WorkManager>,
) {
    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * Requests an opportunistic one-off sync. Uses [ExistingWorkPolicy.REPLACE] so a fresh
     * mutation always starts a new attempt immediately, rather than inheriting the exponential
     * backoff of a previously failed sync (which `APPEND_OR_REPLACE` would).
     */
    fun requestSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()
        workManagerProvider.get()
            .enqueueUniqueWork(SyncWorker.WORK_NAME_ONESHOT, ExistingWorkPolicy.REPLACE, request)
    }

    /** Ensures the recurring safety-net sync exists, keeping any already-scheduled instance. */
    fun ensurePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(PERIODIC_MINUTES, TimeUnit.MINUTES)
            .setConstraints(networkConstraint)
            .build()
        workManagerProvider.get()
            .enqueueUniquePeriodicWork(SyncWorker.WORK_NAME_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private companion object {
        const val PERIODIC_MINUTES = 15L
        const val BACKOFF_SECONDS = 30L
    }
}

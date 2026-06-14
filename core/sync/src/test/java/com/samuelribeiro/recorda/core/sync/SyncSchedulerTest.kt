package com.samuelribeiro.recorda.core.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import javax.inject.Provider

class SyncSchedulerTest {

    private val workManager: WorkManager = mockk(relaxed = true)
    private val scheduler = SyncScheduler(Provider { workManager })

    @Test
    fun `requestSync enqueues a unique one-off work`() {
        scheduler.requestSync()

        verify {
            workManager.enqueueUniqueWork(
                SyncWorker.WORK_NAME_ONESHOT,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>(),
            )
        }
    }

    @Test
    fun `ensurePeriodicSync enqueues a unique periodic work keeping existing`() {
        scheduler.ensurePeriodicSync()

        verify {
            workManager.enqueueUniquePeriodicWork(
                SyncWorker.WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                any<PeriodicWorkRequest>(),
            )
        }
    }
}

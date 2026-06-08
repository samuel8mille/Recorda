package com.samuelribeiro.recorda.logging

import timber.log.Timber
import javax.inject.Inject

/** Debug implementation of [CrashReporter] — logs to Logcat via Timber instead of Crashlytics. */
class LoggingCrashReporter @Inject constructor() : CrashReporter {

    override fun setBuildType(isDebug: Boolean) =
        Timber.d("[CrashReporter] build_type=%s", if (isDebug) "debug" else "release")

    override fun setStoredTopicsCount(count: Int) =
        Timber.d("[CrashReporter] stored_topics_count=%d", count)

    override fun setPendingTopicsCount(count: Int) =
        Timber.d("[CrashReporter] pending_topics_count=%d", count)

    override fun setLastNetworkError(errorType: String) =
        Timber.d("[CrashReporter] last_network_error=%s", errorType)

    override fun logTopicSubmitStarted() = Timber.d("[CrashReporter] topic_submit: started")

    override fun logValidationFailed(errorType: String) =
        Timber.d("[CrashReporter] topic_submit: validation_failed (%s)", errorType)

    override fun logTopicSubmitSuccess(topic: String) =
        Timber.d("[CrashReporter] topic_submit: success (topic=%s)", topic)

    override fun logTopicSubmitFailed(errorType: String) =
        Timber.d("[CrashReporter] topic_submit: failed (%s)", errorType)

    override fun logTopicQueuedOffline() =
        Timber.d("[CrashReporter] topic_submit: queued offline → WorkManager")

    override fun logWorkerSyncStarted(pendingCount: Int) =
        Timber.d("[CrashReporter] worker_sync: started (%d pending)", pendingCount)

    override fun logWorkerSyncSuccess() = Timber.d("[CrashReporter] worker_sync: all topics synced")

    override fun logWorkerSyncFailed() = Timber.d("[CrashReporter] worker_sync: failed, will retry")
}

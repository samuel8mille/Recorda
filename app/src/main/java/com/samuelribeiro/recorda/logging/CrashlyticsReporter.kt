package com.samuelribeiro.recorda.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralises Crashlytics custom keys and breadcrumbs.
 *
 * Custom keys snapshot app state at the moment of a crash.
 * Breadcrumbs (via [FirebaseCrashlytics.log]) form a trail of events leading up to it.
 * Topic contents are never logged — only counts and error types.
 */
@Singleton
class CrashlyticsReporter @Inject constructor() : CrashReporter {

    // ── Custom keys (state snapshot) ─────────────────────────────────────────

    override fun setBuildType(isDebug: Boolean) =
        setKey("build_type", if (isDebug) "debug" else "release")

    override fun setStoredTopicsCount(count: Int) =
        setKey("stored_topics_count", count)

    override fun setPendingTopicsCount(count: Int) =
        setKey("pending_topics_count", count)

    override fun setLastNetworkError(errorType: String) =
        setKey("last_network_error", errorType)

    // ── Breadcrumbs (event trail) ─────────────────────────────────────────────

    override fun logTopicSubmitStarted() = log("topic_submit: started")

    override fun logValidationFailed(errorType: String) = log("topic_submit: validation_failed ($errorType)")

    override fun logTopicSubmitSuccess(topic: String) = log("topic_submit: success (topic=$topic)")

    override fun logTopicSubmitFailed(errorType: String) = log("topic_submit: failed ($errorType)")

    override fun logTopicQueuedOffline() = log("topic_submit: queued offline → WorkManager")

    override fun logWorkerSyncStarted(pendingCount: Int) = log("worker_sync: started ($pendingCount pending)")

    override fun logWorkerSyncSuccess() = log("worker_sync: all topics synced")

    override fun logWorkerSyncFailed() = log("worker_sync: failed, will retry")

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun log(message: String) =
        FirebaseCrashlytics.getInstance().log(message)

    private fun setKey(key: String, value: String) =
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)

    private fun setKey(key: String, value: Int) =
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
}

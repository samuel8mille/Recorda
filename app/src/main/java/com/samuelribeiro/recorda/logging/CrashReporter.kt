package com.samuelribeiro.recorda.logging

/**
 * Client-agnostic contract for crash reporting, custom keys and breadcrumbs.
 *
 * Swap the implementation by changing the Hilt binding in the build-variant
 * DI module — no changes needed in the rest of the codebase.
 *
 * Debug  → [LoggingCrashReporter] (Timber only)
 * Release → [CrashlyticsReporter] (Firebase Crashlytics)
 */
interface CrashReporter {

    // ── Custom keys (state snapshot at crash time) ───────────────────────────
    fun setBuildType(isDebug: Boolean)
    fun setStoredTopicsCount(count: Int)
    fun setPendingTopicsCount(count: Int)
    fun setLastNetworkError(errorType: String)

    // ── Breadcrumbs (event trail leading to the crash) ───────────────────────
    fun logTopicSubmitStarted()
    fun logValidationFailed(errorType: String)
    fun logTopicSubmitSuccess(topic: String)
    fun logTopicSubmitFailed(errorType: String)
    fun logTopicQueuedOffline()
    fun logWorkerSyncStarted(pendingCount: Int)
    fun logWorkerSyncSuccess()
    fun logWorkerSyncFailed()
}

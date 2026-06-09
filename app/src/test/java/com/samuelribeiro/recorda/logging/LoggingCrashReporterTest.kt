package com.samuelribeiro.recorda.logging

import org.junit.Test

class LoggingCrashReporterTest {

    private val reporter = LoggingCrashReporter()

    @Test
    fun `setBuildType does not throw`() {
        reporter.setBuildType(isDebug = true)
        reporter.setBuildType(isDebug = false)
    }

    @Test
    fun `setStoredTopicsCount does not throw`() {
        reporter.setStoredTopicsCount(3)
    }

    @Test
    fun `setPendingTopicsCount does not throw`() {
        reporter.setPendingTopicsCount(1)
    }

    @Test
    fun `setLastNetworkError does not throw`() {
        reporter.setLastNetworkError("timeout")
    }

    @Test
    fun `breadcrumb methods do not throw`() {
        reporter.logTopicSubmitStarted()
        reporter.logValidationFailed("empty")
        reporter.logTopicSubmitSuccess("Kotlin")
        reporter.logTopicSubmitFailed("http_404")
        reporter.logTopicQueuedOffline()
        reporter.logWorkerSyncStarted(2)
        reporter.logWorkerSyncSuccess()
        reporter.logWorkerSyncFailed()
    }
}

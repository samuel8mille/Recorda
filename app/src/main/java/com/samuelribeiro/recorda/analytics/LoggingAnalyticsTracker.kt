package com.samuelribeiro.recorda.analytics

import timber.log.Timber
import javax.inject.Inject

/** [AnalyticsTracker] that logs events to Logcat via Timber. */
class LoggingAnalyticsTracker @Inject constructor() : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) {
        Timber.tag("Analytics").d("%s", event)
    }
}

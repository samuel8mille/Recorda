package com.samuelribeiro.recorda.analytics

interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}

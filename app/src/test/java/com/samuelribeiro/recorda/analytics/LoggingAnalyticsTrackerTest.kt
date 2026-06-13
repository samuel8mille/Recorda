package com.samuelribeiro.recorda.analytics

import org.junit.Test

class LoggingAnalyticsTrackerTest {

    private val tracker = LoggingAnalyticsTracker()

    @Test
    fun `track FlashcardsGenerated does not throw`() {
        tracker.track(AnalyticsEvent.FlashcardsGenerated(5))
    }

    @Test
    fun `track FlashcardsGenerationFailed does not throw`() {
        tracker.track(AnalyticsEvent.FlashcardsGenerationFailed("timeout"))
    }

    @Test
    fun `track EmptyTopicSubmitted does not throw`() {
        tracker.track(AnalyticsEvent.EmptyTopicSubmitted)
    }

    @Test
    fun `track DuplicateTopicSubmitted does not throw`() {
        tracker.track(AnalyticsEvent.DuplicateTopicSubmitted)
    }

    @Test
    fun `track TopicCreated does not throw`() {
        tracker.track(AnalyticsEvent.TopicCreated)
    }
}

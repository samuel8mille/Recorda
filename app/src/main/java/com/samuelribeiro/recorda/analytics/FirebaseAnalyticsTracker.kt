package com.samuelribeiro.recorda.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class FirebaseAnalyticsTracker @Inject constructor(
    private val analytics: FirebaseAnalytics,
) : AnalyticsTracker {

    override fun track(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.TopicCreated ->
                analytics.logEvent("topic_created", null)
            is AnalyticsEvent.FlashcardsGenerated ->
                analytics.logEvent("flashcards_generated", Bundle().apply {
                    putInt("count", event.count)
                })
            is AnalyticsEvent.FlashcardsGenerationFailed ->
                analytics.logEvent("flashcards_generation_failed", Bundle().apply {
                    putString("error_type", event.errorType)
                })
            is AnalyticsEvent.EmptyTopicSubmitted ->
                analytics.logEvent("empty_topic_submitted", null)
            is AnalyticsEvent.DuplicateTopicSubmitted ->
                analytics.logEvent("duplicate_topic_submitted", null)
        }
    }
}

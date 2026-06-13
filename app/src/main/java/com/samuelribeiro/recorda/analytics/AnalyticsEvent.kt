package com.samuelribeiro.recorda.analytics

sealed class AnalyticsEvent {
    /** The user created a new study topic. */
    data object TopicCreated : AnalyticsEvent()
    data class FlashcardsGenerated(val count: Int) : AnalyticsEvent()
    data class FlashcardsGenerationFailed(val errorType: String) : AnalyticsEvent()
    data object EmptyTopicSubmitted : AnalyticsEvent()
    data object DuplicateTopicSubmitted : AnalyticsEvent()
}

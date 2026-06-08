package com.samuelribeiro.recorda.analytics

sealed class AnalyticsEvent {
    data class FlashcardsGenerated(val count: Int) : AnalyticsEvent()
    data class FlashcardsGenerationFailed(val errorType: String) : AnalyticsEvent()
    data object EmptyTopicSubmitted : AnalyticsEvent()
    data object DuplicateTopicSubmitted : AnalyticsEvent()
    data object TopicQueuedOffline : AnalyticsEvent()
}

package com.samuelribeiro.recorda.presentation.navigation

object AppRoute {
    const val TOPIC = "topic"
    const val REVIEW = "review/{topicId}"

    /** Builds the navigation route for the review session of [topicId]. */
    fun review(topicId: String) = "review/$topicId"
}

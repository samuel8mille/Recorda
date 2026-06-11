package com.samuelribeiro.recorda.presentation.navigation

object AppRoute {
    const val TOPIC = "topic"
    const val REVIEW = "review/{topicId}"
    const val MIND_MAP = "mind_map/{topicId}"
    const val STUDY = "study/{topicId}"

    /** Builds the navigation route for the review session of [topicId]. */
    fun review(topicId: String) = "review/$topicId"

    /** Builds the navigation route for the mind map of [topicId]. */
    fun mindMap(topicId: String) = "mind_map/$topicId"

    /** Builds the navigation route for the study guide of [topicId]. */
    fun study(topicId: String) = "study/$topicId"
}

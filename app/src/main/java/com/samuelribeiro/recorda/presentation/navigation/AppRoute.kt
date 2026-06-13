package com.samuelribeiro.recorda.presentation.navigation

object AppRoute {
    const val TOPIC = "topic"
    const val TOPIC_HUB = "topic_hub/{topicId}"
    const val CONTENT = "content/{topicId}"
    const val REVIEW = "review/{topicId}"
    const val MIND_MAP = "mind_map/{topicId}"
    const val STUDY = "study/{topicId}"
    const val STATS = "stats/{topicId}"

    /** Builds the navigation route for the hub of [topicId]. */
    fun topicHub(topicId: String) = "topic_hub/$topicId"

    /** Builds the navigation route for the chapter content of [topicId]. */
    fun content(topicId: String) = "content/$topicId"

    /** Builds the navigation route for the review session of [topicId]. */
    fun review(topicId: String) = "review/$topicId"

    /** Builds the navigation route for the mind map of [topicId]. */
    fun mindMap(topicId: String) = "mind_map/$topicId"

    /** Builds the navigation route for the study guide of [topicId]. */
    fun study(topicId: String) = "study/$topicId"

    /** Builds the navigation route for the retention statistics of [topicId]. */
    fun stats(topicId: String) = "stats/$topicId"
}

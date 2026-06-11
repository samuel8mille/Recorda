package com.samuelribeiro.recorda.domain.prompt

/**
 * Builds the prompt sent to the AI model to generate a study guide for a topic.
 *
 * Keeping prompt construction behind an interface lets prompts evolve (or swap
 * providers) without touching repositories.
 */
interface StudyGuidePromptBuilder {

    /**
     * Builds the study guide generation prompt for [topicName].
     */
    fun build(topicName: String): String
}

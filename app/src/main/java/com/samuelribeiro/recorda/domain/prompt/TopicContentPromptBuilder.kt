package com.samuelribeiro.recorda.domain.prompt

import com.samuelribeiro.recorda.domain.model.Chapter

/**
 * Builds the prompts used to generate a topic's long-form chapter content.
 *
 * Generation happens in two stages: first the chapter list (titles and summaries),
 * then the body of each chapter in its own call — keeping every response small
 * enough to avoid truncation.
 */
interface TopicContentPromptBuilder {

    /** Builds the prompt that generates the chapter list of [topicName]. */
    fun buildChapterList(topicName: String): String

    /** Builds the prompt that generates the long-form body of [chapter] within [topicName]. */
    fun buildChapterBody(topicName: String, chapter: Chapter): String
}

package com.samuelribeiro.recorda.domain.model

/**
 * AI-generated reading guide for a topic, organized into study sections.
 *
 * @property sections The main sub-topics of the subject, in presentation order.
 */
data class StudyGuide(
    val sections: List<StudySection>,
)

/**
 * A single sub-topic of a [StudyGuide], with learning aids for retention.
 *
 * @property id Stable index-based identifier (e.g. "0", "1") used for selection state.
 * @property title The sub-topic's name.
 * @property emoji Single emoji representing the sub-topic, used as a visual anchor.
 * @property definition Short definition of the sub-topic (around two sentences).
 * @property content Full learning content covering the key points in depth.
 * @property summary Short overview of the sub-topic (around two sentences).
 * @property keyPoints Concise bullet points covering the essential facts.
 * @property analogy Everyday analogy that grounds the concept, or `null` when absent.
 * @property example Concrete example of the concept, or `null` when absent.
 * @property mnemonic Short mnemonic rule to aid memorization, or `null` when absent.
 * @property imageUrl Wikipedia thumbnail illustrating the sub-topic, or `null` when none was found.
 */
data class StudySection(
    val id: String,
    val title: String,
    val emoji: String,
    val definition: String,
    val content: String,
    val summary: String,
    val keyPoints: List<String>,
    val analogy: String? = null,
    val example: String? = null,
    val mnemonic: String? = null,
    val imageUrl: String? = null,
)

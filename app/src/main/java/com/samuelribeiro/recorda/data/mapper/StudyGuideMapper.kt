package com.samuelribeiro.recorda.data.mapper

import com.google.gson.Gson
import com.samuelribeiro.recorda.domain.model.StudyGuide
import com.samuelribeiro.recorda.domain.model.StudySection
import javax.inject.Inject

/**
 * Thrown when the AI response cannot be parsed into a valid [StudyGuide].
 */
class StudyGuideParseException(message: String) : Exception(message)

/**
 * Parses Gemini's JSON response into a [StudyGuide].
 *
 * Tolerant to common AI formatting noise: markdown fences and stray text around the
 * JSON object are stripped by extracting the substring between the first `{` and the
 * last `}`. Sections with a blank title are dropped; blank optional fields become
 * `null`. Throws [StudyGuideParseException] when no valid section remains.
 */
class StudyGuideMapper @Inject constructor(
    private val gson: Gson,
) {

    /**
     * Converts [rawText] into a [StudyGuide] with stable index-based section ids.
     */
    fun toStudyGuide(rawText: String): StudyGuide {
        val json = extractJson(rawText)
        val raw = parse(json)
        val sections = (raw.sections.orEmpty())
            .filter { !it.title.isNullOrBlank() }
            .mapIndexed { index, section -> section.toDomain(index) }
        if (sections.isEmpty()) throw StudyGuideParseException("Resposta sem seções válidas")
        return StudyGuide(sections = sections)
    }

    private fun extractJson(rawText: String): String {
        val start = rawText.indexOf('{')
        val end = rawText.lastIndexOf('}')
        if (start == -1 || end <= start) throw StudyGuideParseException("Resposta sem objeto JSON")
        return rawText.substring(start, end + 1)
    }

    private fun parse(json: String): RawGuide =
        runCatching { gson.fromJson(json, RawGuide::class.java) }
            .getOrNull() ?: throw StudyGuideParseException("JSON inválido")

    private fun RawSection.toDomain(index: Int): StudySection = StudySection(
        id = index.toString(),
        title = title.orEmpty().trim(),
        emoji = emoji?.trim().orEmpty(),
        summary = summary?.trim().orEmpty(),
        keyPoints = keyPoints.orEmpty().filter { it.isNotBlank() }.map { it.trim() },
        analogy = analogy?.trim()?.takeIf { it.isNotBlank() },
        example = example?.trim()?.takeIf { it.isNotBlank() },
        mnemonic = mnemonic?.trim()?.takeIf { it.isNotBlank() },
    )

    private data class RawGuide(val sections: List<RawSection>?)

    private data class RawSection(
        val title: String?,
        val emoji: String?,
        val summary: String?,
        val keyPoints: List<String>?,
        val analogy: String?,
        val example: String?,
        val mnemonic: String?,
    )
}

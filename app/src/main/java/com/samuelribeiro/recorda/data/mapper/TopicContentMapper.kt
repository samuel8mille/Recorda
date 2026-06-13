package com.samuelribeiro.recorda.data.mapper

import com.samuelribeiro.recorda.domain.model.Chapter
import javax.inject.Inject

/**
 * Parses Gemini's responses of the chapter content pipeline.
 *
 * The chapter list uses the tolerant line-based format "T: título | S: resumo":
 * lines without both markers are ignored, whitespace and leading numbering are
 * trimmed. Chapter bodies are returned as plain text with markdown fences stripped.
 */
class TopicContentMapper @Inject constructor() {

    /** Converts the chapter list response into [Chapter]s with blank bodies and index ids. */
    fun toChapters(rawText: String): List<Chapter> =
        rawText.lineSequence()
            .mapNotNull(::parseChapterLine)
            .mapIndexed { index, (title, summary) ->
                Chapter(id = index.toString(), title = title, summary = summary)
            }
            .toList()

    /** Sanitizes a chapter body response (trim + markdown fence removal). */
    fun toChapterBody(rawText: String): String =
        rawText.lineSequence()
            .filterNot { it.trim().startsWith("```") }
            .joinToString("\n")
            .trim()

    private fun parseChapterLine(line: String): Pair<String, String>? {
        val titleMarker = line.indexOf("T:")
        val separator = line.indexOf('|')
        val summaryMarker = line.indexOf("S:", startIndex = maxOf(separator, 0))
        if (titleMarker == -1 || separator == -1 || summaryMarker == -1 || separator < titleMarker) return null
        val title = line.substring(titleMarker + 2, separator).trim().trimStart('-', '–').trim()
        val summary = line.substring(summaryMarker + 2).trim()
        if (title.isBlank() || summary.isBlank()) return null
        return title to summary
    }
}

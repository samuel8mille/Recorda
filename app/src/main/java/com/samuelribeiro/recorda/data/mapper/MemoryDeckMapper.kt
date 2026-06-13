package com.samuelribeiro.recorda.data.mapper

import com.samuelribeiro.recorda.domain.model.MemoryCard
import com.samuelribeiro.recorda.domain.model.MemoryDeck
import javax.inject.Inject

/**
 * Parses Gemini's active-recall response into a [MemoryDeck].
 *
 * Uses the tolerant line-based format "C: conceito | D: definição": lines without both
 * markers in the right order are ignored, whitespace and leading numbering are trimmed.
 */
class MemoryDeckMapper @Inject constructor() {

    /** Converts the raw response into a [MemoryDeck] with index-based card ids. */
    fun toMemoryDeck(rawText: String): MemoryDeck =
        MemoryDeck(
            cards = rawText.lineSequence()
                .mapNotNull(::parseCardLine)
                .mapIndexed { index, (concept, definition) ->
                    MemoryCard(id = index.toString(), concept = concept, definition = definition)
                }
                .toList(),
        )

    private fun parseCardLine(line: String): Pair<String, String>? {
        val conceptMarker = line.indexOf("C:")
        val separator = line.indexOf('|')
        val definitionMarker = line.indexOf("D:", startIndex = maxOf(separator, 0))
        if (conceptMarker == -1 || separator == -1 || definitionMarker == -1 || separator < conceptMarker) {
            return null
        }
        val concept = line.substring(conceptMarker + 2, separator).trim().trimStart('-', '–').trim()
        val definition = line.substring(definitionMarker + 2).trim()
        if (concept.isBlank() || definition.isBlank()) return null
        return concept to definition
    }
}

package com.samuelribeiro.recorda.presentation.utils

/**
 * Normalizes a study topic string by trimming surrounding whitespace.
 *
 * @param topic The raw topic string input by the user.
 * @return A normalized topic string.
 */
fun normalizeTopic(topic: String): String {
    return topic.trim()
}

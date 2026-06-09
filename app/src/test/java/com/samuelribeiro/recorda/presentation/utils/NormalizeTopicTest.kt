package com.samuelribeiro.recorda.presentation.utils

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NormalizeTopicTest {

    @Test
    fun `trims leading and trailing whitespace`() {
        assertEquals("Kotlin", normalizeTopic("  Kotlin  "))
    }

    @Test
    fun `preserves inner whitespace`() {
        assertEquals("Segunda Guerra Mundial", normalizeTopic("  Segunda Guerra Mundial  "))
    }

    @Test
    fun `empty string stays empty`() {
        assertTrue(normalizeTopic("").isEmpty())
    }

    @Test
    fun `whitespace-only string becomes empty`() {
        assertTrue(normalizeTopic("   ").isEmpty())
    }

    @Test
    fun `already trimmed string is unchanged`() {
        assertEquals("Kotlin", normalizeTopic("Kotlin"))
    }
}

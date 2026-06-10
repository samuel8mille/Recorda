package com.samuelribeiro.recorda.data.mapper

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MindMapMapperTest {

    private val mapper = MindMapMapper()

    @Test
    fun `parses well-formed three level tree`() {
        val raw = """
            Title
            - Sub1
              - Sub1.1
                - Sub1.1.1
            - Sub2
        """.trimIndent()

        val root = mapper.toMindMap("Topic", raw)

        assertEquals("0", root.id)
        assertEquals("Title", root.title)
        assertEquals(2, root.children.size)

        val sub1 = root.children[0]
        assertEquals("0-0", sub1.id)
        assertEquals("Sub1", sub1.title)
        assertEquals(1, sub1.children.size)

        val sub11 = sub1.children[0]
        assertEquals("0-0-0", sub11.id)
        assertEquals("Sub1.1", sub11.title)
        assertEquals(1, sub11.children.size)

        val sub111 = sub11.children[0]
        assertEquals("0-0-0-0", sub111.id)
        assertEquals("Sub1.1.1", sub111.title)
        assertTrue(sub111.children.isEmpty())

        val sub2 = root.children[1]
        assertEquals("0-1", sub2.id)
        assertEquals("Sub2", sub2.title)
        assertTrue(sub2.children.isEmpty())
    }

    @Test
    fun `flat list becomes direct children of root`() {
        val raw = """
            Title
            - A
            - B
            - C
        """.trimIndent()

        val root = mapper.toMindMap("Topic", raw)

        assertEquals("Title", root.title)
        assertEquals(listOf("A", "B", "C"), root.children.map { it.title })
        assertEquals(listOf("0-0", "0-1", "0-2"), root.children.map { it.id })
        assertTrue(root.children.all { it.children.isEmpty() })
    }

    @Test
    fun `single line becomes root without children`() {
        val root = mapper.toMindMap("Topic", "Title only")

        assertEquals("0", root.id)
        assertEquals("Title only", root.title)
        assertTrue(root.children.isEmpty())
    }

    @Test
    fun `blank text falls back to topic name as root`() {
        val root = mapper.toMindMap("Topic", "   \n   \n")

        assertEquals("0", root.id)
        assertEquals("Topic", root.title)
        assertTrue(root.children.isEmpty())
    }

    @Test
    fun `indentation jump greater than one level still nests under last node`() {
        val raw = """
            Title
            - A
                  - B
        """.trimIndent()

        val root = mapper.toMindMap("Topic", raw)

        val a = root.children.single()
        assertEquals("A", a.title)
        assertEquals("0-0", a.id)

        val b = a.children.single()
        assertEquals("B", b.title)
        assertEquals("0-0-0", b.id)
    }

    @Test
    fun `lines without dash prefix are used as-is`() {
        val raw = """
            Title
            Sub A
            Sub B
        """.trimIndent()

        val root = mapper.toMindMap("Topic", raw)

        assertEquals("Title", root.title)
        assertEquals(listOf("Sub A", "Sub B"), root.children.map { it.title })
    }
}

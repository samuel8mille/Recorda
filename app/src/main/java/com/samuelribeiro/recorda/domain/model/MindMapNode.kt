package com.samuelribeiro.recorda.domain.model

/**
 * A node in a topic's mind map tree.
 *
 * @property id Stable, path-based identifier (e.g. "0", "0-1", "0-1-0") used for expand/collapse state.
 * @property title The node's label.
 * @property children Sub-topics nested under this node.
 */
data class MindMapNode(
    val id: String,
    val title: String,
    val children: List<MindMapNode> = emptyList(),
)

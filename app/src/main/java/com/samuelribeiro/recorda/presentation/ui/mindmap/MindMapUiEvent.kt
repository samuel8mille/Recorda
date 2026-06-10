package com.samuelribeiro.recorda.presentation.ui.mindmap

/** User-initiated events on the mind map screen. */
sealed class MindMapUiEvent

/**
 * Triggered when the user taps a node to expand or collapse its children.
 *
 * @property nodeId The ID of the node being toggled.
 */
data class ToggleNode(val nodeId: String) : MindMapUiEvent()

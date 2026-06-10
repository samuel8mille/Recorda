package com.samuelribeiro.recorda.feature.mindmap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ProcessUiState
import com.samuelribeiro.recorda.core.ui.ProcessContainer
import com.samuelribeiro.recorda.domain.model.MindMapNode
import com.samuelribeiro.recorda.presentation.ui.mindmap.MindMapUiState
import com.samuelribeiro.recorda.presentation.ui.mindmap.MindMapViewModel
import com.samuelribeiro.recorda.presentation.ui.mindmap.ToggleNode
import com.samuelribeiro.recorda.ui.theme.HorizontalPadding
import com.samuelribeiro.recorda.ui.theme.SpaceMedium

private val IndentStep = 16.dp

@Composable
fun MindMapScreen(viewModel: MindMapViewModel, onNavigateBack: () -> Unit) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    MindMapContent(
        uiState = uiState,
        onToggleNode = { nodeId -> viewModel.onSendEvent(ToggleNode(nodeId)) },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MindMapContent(
    uiState: ProcessUiState<MindMapUiState>,
    onToggleNode: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.content.topicName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ProcessContainer(uiState = uiState) {
                val rootNode = uiState.content.rootNode
                if (rootNode != null) {
                    MindMapTree(
                        root = rootNode,
                        expandedIds = uiState.content.expandedIds,
                        onToggleNode = onToggleNode,
                    )
                } else if (uiState.error != null) {
                    ErrorMessage()
                }
            }
        }
    }
}

@Composable
private fun MindMapTree(
    root: MindMapNode,
    expandedIds: Set<String>,
    onToggleNode: (String) -> Unit,
) {
    val nodes = remember(root, expandedIds) { flattenTree(root, expandedIds) }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = nodes, key = { (node, _) -> node.id }) { (node, depth) ->
            MindMapNodeRow(
                node = node,
                depth = depth,
                isExpanded = node.id in expandedIds,
                onToggleNode = onToggleNode,
            )
        }
    }
}

@Composable
private fun MindMapNodeRow(
    node: MindMapNode,
    depth: Int,
    isExpanded: Boolean,
    onToggleNode: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HorizontalPadding, vertical = SpaceMedium / 4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(IndentStep * depth))
        if (node.children.isEmpty()) {
            Spacer(modifier = Modifier.size(48.dp))
        } else {
            IconButton(onClick = { onToggleNode(node.id) }) {
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.AutoMirrored.Filled.KeyboardArrowRight
                    },
                    contentDescription = stringResource(
                        if (isExpanded) {
                            R.string.mind_map_collapse_description
                        } else {
                            R.string.mind_map_expand_description
                        },
                    ),
                )
            }
        }
        Text(
            text = node.title,
            style = if (depth == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ErrorMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(HorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.mind_map_error_generation),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Flattens [root] into a depth-annotated list for rendering in a [LazyColumn].
 *
 * Children of a node are only included when the node's ID is in [expandedIds].
 */
internal fun flattenTree(
    root: MindMapNode,
    expandedIds: Set<String>,
    depth: Int = 0,
): List<Pair<MindMapNode, Int>> {
    val result = mutableListOf(root to depth)
    if (root.id in expandedIds) {
        root.children.forEach { child -> result += flattenTree(child, expandedIds, depth + 1) }
    }
    return result
}

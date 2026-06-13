package com.samuelribeiro.recorda.presentation.ui.topic.composables

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ProcessUiState
import com.samuelribeiro.recorda.core.ui.ProcessContainer
import com.samuelribeiro.recorda.presentation.ui.topic.ConfirmDeleteTopic
import com.samuelribeiro.recorda.presentation.ui.topic.DismissDeleteDialog
import com.samuelribeiro.recorda.presentation.ui.topic.OnAddTopicClick
import com.samuelribeiro.recorda.presentation.ui.topic.RequestDeleteTopic
import com.samuelribeiro.recorda.presentation.ui.topic.TopicUiState
import com.samuelribeiro.recorda.presentation.ui.topic.TopicViewModel

@Composable
fun TopicScreen(
    viewModel: TopicViewModel,
    onNavigateToTopicHub: (String) -> Unit,
) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()

    TopicScreen(
        uiState = uiState,
        onAddTopicClick = { topic -> viewModel.onSendEvent(OnAddTopicClick(topic)) },
        onTopicClick = onNavigateToTopicHub,
        onDeleteClick = { topicId -> viewModel.onSendEvent(RequestDeleteTopic(topicId)) },
        onConfirmDelete = { viewModel.onSendEvent(ConfirmDeleteTopic) },
        onDismissDelete = { viewModel.onSendEvent(DismissDeleteDialog) },
    )
}

@Composable
fun TopicScreen(
    uiState: ProcessUiState<TopicUiState>,
    onAddTopicClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
) {
    ProcessContainer(uiState = uiState) {
        TopicScaffold(
            uiState = uiState.content,
            onAddTopicClick = onAddTopicClick,
            onTopicClick = onTopicClick,
            onDeleteClick = onDeleteClick,
            onConfirmDelete = onConfirmDelete,
            onDismissDelete = onDismissDelete,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicScaffold(
    uiState: TopicUiState,
    onAddTopicClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.semantics { heading() },
                        text = stringResource(R.string.app_name),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxHeight()
        ) {
            TopicListContent(
                uiState = uiState,
                onAddTopicClick = onAddTopicClick,
                onTopicClick = onTopicClick,
                onDeleteClick = onDeleteClick,
                onConfirmDelete = onConfirmDelete,
                onDismissDelete = onDismissDelete,
            )
        }
    }
}

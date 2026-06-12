package com.samuelribeiro.recorda.presentation.ui.topic.composables

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ProcessUiState
import com.samuelribeiro.recorda.core.ui.ProcessContainer
import com.samuelribeiro.recorda.presentation.ui.mapper.asUserMessage
import com.samuelribeiro.recorda.presentation.ui.topic.ConfirmDeleteTopic
import com.samuelribeiro.recorda.presentation.ui.topic.DismissDeleteDialog
import com.samuelribeiro.recorda.presentation.ui.topic.NavigateToReview
import com.samuelribeiro.recorda.presentation.ui.topic.OnGenerateFlashcardsClick
import com.samuelribeiro.recorda.presentation.ui.topic.RequestDeleteTopic
import com.samuelribeiro.recorda.presentation.ui.topic.ShowError
import com.samuelribeiro.recorda.presentation.ui.topic.TopicUiState
import com.samuelribeiro.recorda.presentation.ui.topic.TopicViewModel

@Composable
fun TopicScreen(
    viewModel: TopicViewModel,
    onNavigateToReview: (String) -> Unit,
    onNavigateToMindMap: (String) -> Unit,
    onNavigateToStudy: (String) -> Unit,
    onNavigateToStats: (String) -> Unit,
) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.loading) {
        if (uiState.loading != null) snackbarHostState.currentSnackbarData?.dismiss()
    }

    LaunchedEffect(Unit) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                is ShowError -> snackbarHostState.showSnackbar(
                    message = effect.error.asUserMessage(context),
                    duration = SnackbarDuration.Long,
                )
                is NavigateToReview -> onNavigateToReview(effect.topicId)
            }
        }
    }

    TopicScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onGenerateFlashcardsClick = { topic -> viewModel.onSendEvent(OnGenerateFlashcardsClick(topic)) },
        onReviewClick = onNavigateToReview,
        onMindMapClick = onNavigateToMindMap,
        onStudyClick = onNavigateToStudy,
        onStatsClick = onNavigateToStats,
        onDeleteClick = { topicId -> viewModel.onSendEvent(RequestDeleteTopic(topicId)) },
        onConfirmDelete = { viewModel.onSendEvent(ConfirmDeleteTopic) },
        onDismissDelete = { viewModel.onSendEvent(DismissDeleteDialog) },
    )
}

@Composable
fun TopicScreen(
    uiState: ProcessUiState<TopicUiState>,
    snackbarHostState: SnackbarHostState,
    onGenerateFlashcardsClick: (String) -> Unit,
    onReviewClick: (String) -> Unit,
    onMindMapClick: (String) -> Unit,
    onStudyClick: (String) -> Unit,
    onStatsClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
) {
    ProcessContainer(uiState = uiState) {
        TopicScaffold(
            uiState = uiState.content,
            snackbarHostState = snackbarHostState,
            onGenerateFlashcardsClick = onGenerateFlashcardsClick,
            onReviewClick = onReviewClick,
            onMindMapClick = onMindMapClick,
            onStudyClick = onStudyClick,
            onStatsClick = onStatsClick,
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
    snackbarHostState: SnackbarHostState,
    onGenerateFlashcardsClick: (String) -> Unit,
    onReviewClick: (String) -> Unit,
    onMindMapClick: (String) -> Unit,
    onStudyClick: (String) -> Unit,
    onStatsClick: (String) -> Unit,
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                )
            }
        },
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxHeight()
        ) {
            TopicContent(
                uiState = uiState,
                onGenerateFlashcardsClick = onGenerateFlashcardsClick,
                onReviewClick = onReviewClick,
                onMindMapClick = onMindMapClick,
                onStudyClick = onStudyClick,
                onStatsClick = onStatsClick,
                onDeleteClick = onDeleteClick,
                onConfirmDelete = onConfirmDelete,
                onDismissDelete = onDismissDelete,
            )
        }
    }
}

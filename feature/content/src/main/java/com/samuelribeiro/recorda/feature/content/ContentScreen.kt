package com.samuelribeiro.recorda.feature.content

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ProcessUiState
import com.samuelribeiro.recorda.core.ui.ProcessContainer
import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.presentation.ui.content.CloseChapter
import com.samuelribeiro.recorda.presentation.ui.content.ContentUiState
import com.samuelribeiro.recorda.presentation.ui.content.ContentViewModel
import com.samuelribeiro.recorda.presentation.ui.content.RetryGeneration
import com.samuelribeiro.recorda.presentation.ui.content.SelectChapter
import com.samuelribeiro.recorda.ui.theme.HorizontalPadding
import com.samuelribeiro.recorda.ui.theme.SpaceMedium
import com.samuelribeiro.recorda.ui.theme.SpaceSmall

const val CHAPTER_CARD_TEST_TAG = "ChapterCardTestTag"

@Composable
fun ContentScreen(viewModel: ContentViewModel, onNavigateBack: () -> Unit) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    BackHandler(enabled = uiState.content.selectedChapterId != null) {
        viewModel.onSendEvent(CloseChapter)
    }
    ContentScreen(
        uiState = uiState,
        onSelectChapter = { chapterId -> viewModel.onSendEvent(SelectChapter(chapterId)) },
        onCloseChapter = { viewModel.onSendEvent(CloseChapter) },
        onRetry = { viewModel.onSendEvent(RetryGeneration) },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContentScreen(
    uiState: ProcessUiState<ContentUiState>,
    onSelectChapter: (String) -> Unit,
    onCloseChapter: () -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val selectedChapter = uiState.content.selectedChapter
    Scaffold(
        topBar = {
            ContentTopBar(
                topicName = uiState.content.topicName,
                isDetailOpen = selectedChapter != null,
                onCloseChapter = onCloseChapter,
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ProcessContainer(uiState = uiState) {
                when {
                    selectedChapter != null -> ChapterDetail(chapter = selectedChapter)
                    uiState.error != null -> ErrorMessage(onRetry = onRetry)
                    else -> ChapterList(
                        chapters = uiState.content.content?.chapters.orEmpty(),
                        progress = uiState.content.generationProgress,
                        onSelectChapter = onSelectChapter,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentTopBar(
    topicName: String,
    isDetailOpen: Boolean,
    onCloseChapter: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = topicName) },
        navigationIcon = {
            IconButton(onClick = if (isDetailOpen) onCloseChapter else onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(
                        if (isDetailOpen) R.string.content_close_detail_description else R.string.content_back_description,
                    ),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@Composable
private fun ChapterList(
    chapters: List<Chapter>,
    progress: com.samuelribeiro.recorda.presentation.ui.content.ChapterProgress?,
    onSelectChapter: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = HorizontalPadding, vertical = SpaceMedium),
        verticalArrangement = Arrangement.spacedBy(SpaceSmall),
    ) {
        items(items = chapters, key = { it.id }) { chapter ->
            ChapterCard(
                chapter = chapter,
                isGenerating = chapter.body.isBlank() && progress != null,
                progress = progress,
                onSelectChapter = onSelectChapter,
            )
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: Chapter,
    isGenerating: Boolean,
    progress: com.samuelribeiro.recorda.presentation.ui.content.ChapterProgress?,
    onSelectChapter: (String) -> Unit,
) {
    val isReady = chapter.body.isNotBlank()
    ElevatedCard(
        modifier = Modifier
            .testTag(CHAPTER_CARD_TEST_TAG)
            .fillMaxWidth()
            .then(
                if (isReady) {
                    Modifier.clickable(onClickLabel = stringResource(R.string.content_chapter_open_description)) {
                        onSelectChapter(chapter.id)
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = chapter.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = chapter.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                if (isGenerating && progress != null) {
                    Spacer(Modifier.size(SpaceSmall))
                    Text(
                        text = stringResource(
                            R.string.content_chapter_generating,
                            progress.current,
                            progress.total,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (isGenerating) {
                Spacer(Modifier.width(SpaceMedium))
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ErrorMessage(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.content_error_generation),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(SpaceMedium))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.content_retry_button))
        }
    }
}

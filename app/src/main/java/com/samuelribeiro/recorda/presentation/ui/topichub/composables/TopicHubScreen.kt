package com.samuelribeiro.recorda.presentation.ui.topichub.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.presentation.ui.topichub.TopicHubViewModel
import com.samuelribeiro.recorda.ui.theme.HorizontalPadding
import com.samuelribeiro.recorda.ui.theme.SpaceMedium
import com.samuelribeiro.recorda.ui.theme.SpaceSmall

const val HUB_CONTENT_TEST_TAG = "HubContentTestTag"
const val HUB_STUDY_TEST_TAG = "HubStudyTestTag"
const val HUB_MIND_MAP_TEST_TAG = "HubMindMapTestTag"
const val HUB_REVIEW_TEST_TAG = "HubReviewTestTag"
const val HUB_STATS_TEST_TAG = "HubStatsTestTag"

private data class HubItem(
    val testTag: String,
    val labelRes: Int,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

/**
 * Grid of the learning materials available for a topic. Pure navigation: each card routes
 * to its corresponding feature for [TopicHubViewModel]'s topic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicHubScreen(
    viewModel: TopicHubViewModel,
    onNavigateToContent: () -> Unit,
    onNavigateToStudy: () -> Unit,
    onNavigateToMindMap: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val items = listOf(
        HubItem(HUB_CONTENT_TEST_TAG, R.string.hub_item_content, Icons.AutoMirrored.Filled.List, onNavigateToContent),
        HubItem(HUB_STUDY_TEST_TAG, R.string.hub_item_study, Icons.Filled.Star, onNavigateToStudy),
        HubItem(HUB_MIND_MAP_TEST_TAG, R.string.hub_item_mind_map, Icons.Filled.Share, onNavigateToMindMap),
        HubItem(HUB_REVIEW_TEST_TAG, R.string.hub_item_review, Icons.Filled.Refresh, onNavigateToReview),
        HubItem(HUB_STATS_TEST_TAG, R.string.hub_item_stats, Icons.Filled.DateRange, onNavigateToStats),
    )

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
                        text = uiState.content.topicName.ifBlank {
                            stringResource(R.string.topic_hub_screen_title)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.topic_hub_back_description),
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = HorizontalPadding, vertical = SpaceMedium),
            horizontalArrangement = Arrangement.spacedBy(SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(SpaceMedium),
        ) {
            items(items = items) { item ->
                HubCard(item = item)
            }
        }
    }
}

@Composable
private fun HubCard(item: HubItem) {
    ElevatedCard(
        modifier = Modifier
            .testTag(item.testTag)
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = item.onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SpaceMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = SpaceSmall),
            )
            Text(
                text = stringResource(item.labelRes),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

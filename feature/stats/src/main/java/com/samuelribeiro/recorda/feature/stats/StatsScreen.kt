package com.samuelribeiro.recorda.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ProcessUiState
import com.samuelribeiro.recorda.core.ui.ProcessContainer
import com.samuelribeiro.recorda.domain.model.TopicStats
import com.samuelribeiro.recorda.presentation.ui.stats.RetryLoad
import com.samuelribeiro.recorda.presentation.ui.stats.StatsUiState
import com.samuelribeiro.recorda.presentation.ui.stats.StatsViewModel
import com.samuelribeiro.recorda.ui.theme.HorizontalPadding
import com.samuelribeiro.recorda.ui.theme.SpaceLarge
import com.samuelribeiro.recorda.ui.theme.SpaceMedium
import kotlin.math.roundToInt

@Composable
fun StatsScreen(viewModel: StatsViewModel, onNavigateBack: () -> Unit) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    StatsContent(
        uiState = uiState,
        onRetry = { viewModel.onSendEvent(RetryLoad) },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatsContent(
    uiState: ProcessUiState<StatsUiState>,
    onRetry: () -> Unit,
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
                            contentDescription = stringResource(R.string.stats_back_description),
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
                val stats = uiState.content.stats
                when {
                    stats != null -> StatsList(stats = stats)
                    uiState.error != null -> ErrorMessage(onRetry = onRetry)
                }
            }
        }
    }
}

@Composable
private fun StatsList(stats: TopicStats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HorizontalPadding, vertical = SpaceMedium),
    ) {
        MetricRow(labelRes = R.string.stats_total_cards, value = stats.totalCards.toString())
        MetricRow(labelRes = R.string.stats_cards_on_track, value = stats.cardsOnTrack.toString())
        MetricRow(labelRes = R.string.stats_cards_due, value = stats.cardsDue.toString())
        MetricRow(labelRes = R.string.stats_cards_never_reviewed, value = stats.cardsNeverReviewed.toString())
        MetricRow(labelRes = R.string.stats_success_rate, value = stats.successRate.asPercentText())
        MetricRow(
            labelRes = R.string.stats_streak_days,
            value = stringResource(R.string.stats_streak_value, stats.streakDays),
        )
        MetricRow(labelRes = R.string.stats_average_ease, value = stats.averageEaseFactor.asEaseText())
        Spacer(Modifier.height(SpaceLarge))
        Text(
            text = stringResource(R.string.stats_chart_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(SpaceMedium))
        WeeklyReviewBarChart(reviewsPerDay = stats.reviewsPerDay)
        Spacer(Modifier.height(SpaceLarge))
    }
}

@Composable
private fun MetricRow(labelRes: Int, value: String) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = SpaceMedium / 2)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(labelRes), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
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
            text = stringResource(R.string.stats_error_loading),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(SpaceMedium))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.stats_retry_button))
        }
    }
}

@Composable
private fun Float?.asPercentText(): String =
    if (this == null) {
        stringResource(R.string.stats_empty_value)
    } else {
        stringResource(R.string.stats_percent_value, (this * PERCENT_FACTOR).roundToInt())
    }

@Composable
private fun Float?.asEaseText(): String =
    if (this == null) {
        stringResource(R.string.stats_empty_value)
    } else {
        ((this * EASE_ROUNDING).roundToInt() / EASE_ROUNDING).toString()
    }

private const val PERCENT_FACTOR = 100
private const val EASE_ROUNDING = 100f

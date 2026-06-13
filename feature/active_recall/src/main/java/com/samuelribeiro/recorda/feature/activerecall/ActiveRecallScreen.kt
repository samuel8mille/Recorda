package com.samuelribeiro.recorda.feature.activerecall

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ProcessUiState
import com.samuelribeiro.recorda.core.ui.ProcessContainer
import com.samuelribeiro.recorda.domain.model.MemoryCard
import com.samuelribeiro.recorda.domain.model.OralAnswerEvaluation
import com.samuelribeiro.recorda.domain.model.OralAnswerVerdict
import com.samuelribeiro.recorda.presentation.ui.activerecall.ActiveRecallUiState
import com.samuelribeiro.recorda.presentation.ui.activerecall.ActiveRecallViewModel
import com.samuelribeiro.recorda.presentation.ui.activerecall.NextCard
import com.samuelribeiro.recorda.presentation.ui.activerecall.RecallPhase
import com.samuelribeiro.recorda.presentation.ui.activerecall.RetryGeneration
import com.samuelribeiro.recorda.ui.theme.HorizontalPadding
import com.samuelribeiro.recorda.ui.theme.SpaceLarge
import com.samuelribeiro.recorda.ui.theme.SpaceMedium

const val RECALL_CARD_TEST_TAG = "RecallCardTestTag"

@Composable
fun ActiveRecallScreen(viewModel: ActiveRecallViewModel, onNavigateBack: () -> Unit) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    ActiveRecallContent(
        uiState = uiState,
        onNextCard = { viewModel.onSendEvent(NextCard) },
        onRetry = { viewModel.onSendEvent(RetryGeneration) },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActiveRecallContent(
    uiState: ProcessUiState<ActiveRecallUiState>,
    onNextCard: () -> Unit,
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
                            contentDescription = stringResource(R.string.active_recall_back_description),
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
                when {
                    uiState.content.isSessionComplete -> SessionComplete(onNavigateBack = onNavigateBack)
                    uiState.error != null -> ErrorMessage(onRetry = onRetry)
                    uiState.content.currentCard != null -> RecallCard(
                        card = uiState.content.currentCard!!,
                        phase = uiState.content.phase,
                        evaluation = uiState.content.evaluation,
                        onNextCard = onNextCard,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecallCard(
    card: MemoryCard,
    phase: RecallPhase,
    evaluation: OralAnswerEvaluation?,
    onNextCard: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = HorizontalPadding, vertical = SpaceMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ElevatedCard(
            modifier = Modifier
                .testTag(RECALL_CARD_TEST_TAG)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpaceLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpaceMedium),
            ) {
                Text(text = card.concept, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                if (phase == RecallPhase.Showing) {
                    Text(
                        text = card.definition,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Spacer(Modifier.height(SpaceLarge))
        PhaseIndicator(phase = phase, evaluation = evaluation, onNextCard = onNextCard)
    }
}

@Composable
private fun PhaseIndicator(
    phase: RecallPhase,
    evaluation: OralAnswerEvaluation?,
    onNextCard: () -> Unit,
) {
    when (phase) {
        RecallPhase.Showing -> Text(
            text = stringResource(R.string.active_recall_show_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        RecallPhase.Recording -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(SpaceMedium))
            Text(text = stringResource(R.string.active_recall_recording), style = MaterialTheme.typography.bodyMedium)
        }
        RecallPhase.Evaluating -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
        RecallPhase.Result -> Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpaceMedium),
        ) {
            evaluation?.let { VerdictBanner(it) }
            Button(onClick = onNextCard) {
                Text(text = stringResource(R.string.active_recall_next_button))
            }
        }
    }
}

@Composable
private fun VerdictBanner(evaluation: OralAnswerEvaluation) {
    val (containerColor, titleRes) = when (evaluation.verdict) {
        OralAnswerVerdict.CORRECT -> MaterialTheme.colorScheme.primaryContainer to R.string.review_oral_verdict_correct
        OralAnswerVerdict.PARTIAL -> MaterialTheme.colorScheme.tertiaryContainer to R.string.review_oral_verdict_partial
        OralAnswerVerdict.INCORRECT -> MaterialTheme.colorScheme.errorContainer to R.string.review_oral_verdict_incorrect
    }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(SpaceMedium / 4),
        ) {
            Text(text = stringResource(titleRes), style = MaterialTheme.typography.titleMedium)
            Text(text = evaluation.feedback, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SessionComplete(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(HorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpaceMedium),
        ) {
            Text(
                text = stringResource(R.string.active_recall_complete_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onNavigateBack) {
                Text(text = stringResource(R.string.active_recall_complete_button))
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
            text = stringResource(R.string.active_recall_error_generation),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(SpaceMedium))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.active_recall_retry_button))
        }
    }
}

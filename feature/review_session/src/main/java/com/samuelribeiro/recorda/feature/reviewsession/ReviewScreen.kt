package com.samuelribeiro.recorda.feature.reviewsession

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.OralAnswerEvaluation
import com.samuelribeiro.recorda.domain.model.OralAnswerVerdict
import com.samuelribeiro.recorda.presentation.ui.review.FlipCard
import com.samuelribeiro.recorda.presentation.ui.review.RateCard
import com.samuelribeiro.recorda.presentation.ui.review.ReviewUiState
import com.samuelribeiro.recorda.presentation.ui.review.ReviewViewModel
import com.samuelribeiro.recorda.presentation.ui.review.StartOralAnswer
import com.samuelribeiro.recorda.ui.theme.HorizontalPadding
import com.samuelribeiro.recorda.ui.theme.SpaceLarge
import com.samuelribeiro.recorda.ui.theme.SpaceMedium

@Composable
fun ReviewScreen(viewModel: ReviewViewModel, onNavigateBack: () -> Unit) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) viewModel.onSendEvent(StartOralAnswer) }
    ReviewContent(
        uiState = uiState.content,
        onFlipCard = { viewModel.onSendEvent(FlipCard) },
        onRateCard = { rating -> viewModel.onSendEvent(RateCard(rating)) },
        onStartOralAnswer = {
            val isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
            if (isGranted) {
                viewModel.onSendEvent(StartOralAnswer)
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewContent(
    uiState: ReviewUiState,
    onFlipCard: () -> Unit,
    onRateCard: (CardRating) -> Unit,
    onStartOralAnswer: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    if (uiState.isNothingDue) {
        NothingDueContent(topicName = uiState.topicName, onNavigateBack = onNavigateBack)
        return
    }
    if (uiState.isSessionComplete) {
        SessionCompleteContent(topicName = uiState.topicName, onNavigateBack = onNavigateBack)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.topicName) },
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
        if (uiState.flashcards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = HorizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(SpaceMedium))
                Text(
                    text = stringResource(
                        R.string.review_progress,
                        uiState.currentIndex + 1,
                        uiState.flashcards.size,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(SpaceLarge))
                FlashcardView(
                    flashcard = uiState.flashcards[uiState.currentIndex],
                    isFlipped = uiState.isFlipped,
                    onClick = onFlipCard,
                )
                Spacer(Modifier.height(SpaceLarge))
                uiState.oralEvaluation?.let { evaluation ->
                    OralEvaluationBanner(evaluation = evaluation)
                    Spacer(Modifier.height(SpaceMedium))
                }
                if (uiState.isFlipped) {
                    RatingButtons(onRateCard = onRateCard)
                } else if (uiState.isListening) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpaceMedium),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text(
                            text = stringResource(R.string.review_oral_listening),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.review_tap_to_reveal),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                        IconButton(onClick = onStartOralAnswer) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.review_oral_mic_description),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardView(
    flashcard: Flashcard,
    isFlipped: Boolean,
    onClick: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "flip_rotation",
    )
    val isShowingBack = rotation > 90f

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(SpaceLarge)
                .graphicsLayer { rotationY = if (isShowingBack) 180f else 0f },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isShowingBack) flashcard.answer else flashcard.question,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun OralEvaluationBanner(evaluation: OralAnswerEvaluation) {
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
private fun RatingButtons(onRateCard: (CardRating) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        OutlinedButton(onClick = { onRateCard(CardRating.AGAIN) }) {
            Text(text = stringResource(R.string.review_rating_again))
        }
        Button(onClick = { onRateCard(CardRating.GOOD) }) {
            Text(text = stringResource(R.string.review_rating_good))
        }
        FilledTonalButton(onClick = { onRateCard(CardRating.EASY) }) {
            Text(text = stringResource(R.string.review_rating_easy))
        }
    }
}

@Composable
private fun NothingDueContent(topicName: String, onNavigateBack: () -> Unit) {
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
                text = stringResource(R.string.review_nothing_due_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.review_nothing_due_subtitle, topicName),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(SpaceLarge))
            Button(onClick = onNavigateBack) {
                Text(text = stringResource(R.string.review_nothing_due_button))
            }
        }
    }
}

@Composable
private fun SessionCompleteContent(topicName: String, onNavigateBack: () -> Unit) {
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
                text = stringResource(R.string.review_complete_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.review_complete_subtitle, topicName),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(SpaceLarge))
            Button(onClick = onNavigateBack) {
                Text(text = stringResource(R.string.review_complete_button))
            }
        }
    }
}

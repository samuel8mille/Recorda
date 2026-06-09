package com.samuelribeiro.recorda.feature.reviewsession

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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.presentation.ui.review.FlipCard
import com.samuelribeiro.recorda.presentation.ui.review.RateCard
import com.samuelribeiro.recorda.presentation.ui.review.ReviewUiState
import com.samuelribeiro.recorda.presentation.ui.review.ReviewViewModel
import com.samuelribeiro.recorda.ui.theme.HorizontalPadding
import com.samuelribeiro.recorda.ui.theme.SpaceLarge
import com.samuelribeiro.recorda.ui.theme.SpaceMedium

@Composable
fun ReviewScreen(viewModel: ReviewViewModel, onNavigateBack: () -> Unit) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    ReviewContent(
        uiState = uiState.content,
        onFlipCard = { viewModel.onSendEvent(FlipCard) },
        onRateCard = { rating -> viewModel.onSendEvent(RateCard(rating)) },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewContent(
    uiState: ReviewUiState,
    onFlipCard: () -> Unit,
    onRateCard: (CardRating) -> Unit,
    onNavigateBack: () -> Unit,
) {
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
                if (uiState.isFlipped) {
                    RatingButtons(onRateCard = onRateCard)
                } else {
                    Text(
                        text = stringResource(R.string.review_tap_to_reveal),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
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

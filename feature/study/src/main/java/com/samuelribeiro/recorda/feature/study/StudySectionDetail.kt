package com.samuelribeiro.recorda.feature.study

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.domain.model.StudySection
import com.samuelribeiro.recorda.ui.theme.HorizontalPadding
import com.samuelribeiro.recorda.ui.theme.SpaceLarge
import com.samuelribeiro.recorda.ui.theme.SpaceMedium

private val HeaderImageHeight = 180.dp

@Composable
internal fun StudySectionDetail(section: StudySection) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HorizontalPadding, vertical = SpaceMedium),
    ) {
        SectionHeaderImage(section = section)
        Spacer(Modifier.height(SpaceMedium))
        Text(text = section.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(SpaceMedium))
        Text(text = section.summary, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(SpaceLarge))
        KeyPointsList(keyPoints = section.keyPoints)
        section.analogy?.let {
            HighlightBox(
                titleRes = R.string.study_analogy_title,
                text = it,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            )
        }
        section.example?.let {
            HighlightBox(
                titleRes = R.string.study_example_title,
                text = it,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            )
        }
        section.mnemonic?.let {
            HighlightBox(
                titleRes = R.string.study_mnemonic_title,
                text = it,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
        }
        Spacer(Modifier.height(SpaceLarge))
    }
}

@Composable
private fun SectionHeaderImage(section: StudySection) {
    if (section.imageUrl == null) {
        SectionHeaderEmoji(emoji = section.emoji)
    } else {
        SubcomposeAsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderImageHeight),
            model = section.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = { SectionHeaderEmoji(emoji = section.emoji) },
            error = { SectionHeaderEmoji(emoji = section.emoji) },
        )
    }
}

@Composable
private fun SectionHeaderEmoji(emoji: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderImageHeight),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, style = MaterialTheme.typography.displayLarge)
    }
}

@Composable
private fun KeyPointsList(keyPoints: List<String>) {
    if (keyPoints.isEmpty()) return
    Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(
            text = stringResource(R.string.study_key_points_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(SpaceMedium / 2))
        keyPoints.forEachIndexed { index, point ->
            Text(
                modifier = Modifier.padding(bottom = SpaceMedium / 2),
                text = "${index + 1}. $point",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    Spacer(Modifier.height(SpaceMedium))
}

@Composable
private fun HighlightBox(
    @StringRes titleRes: Int,
    text: String,
    containerColor: Color,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
    ) {
        Column(modifier = Modifier.padding(SpaceMedium)) {
            Text(text = stringResource(titleRes), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(SpaceMedium / 4))
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
    Spacer(Modifier.height(SpaceMedium))
}

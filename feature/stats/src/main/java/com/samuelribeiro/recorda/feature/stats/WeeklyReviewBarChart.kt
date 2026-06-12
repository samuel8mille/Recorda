package com.samuelribeiro.recorda.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.domain.model.DailyReviewCount
import com.samuelribeiro.recorda.ui.theme.SpaceMedium
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val ChartHeight = 120.dp
private const val BarWidthFraction = 0.5f
private const val MinVisibleBarFraction = 0.02f

@Composable
internal fun WeeklyReviewBarChart(reviewsPerDay: List<DailyReviewCount>) {
    val max = reviewsPerDay.maxOfOrNull { it.count } ?: 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        reviewsPerDay.forEach { daily ->
            DayBar(
                daily = daily,
                maxCount = max,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DayBar(
    daily: DailyReviewCount,
    maxCount: Int,
    modifier: Modifier = Modifier,
) {
    val dayFormatted = daily.date.format(DateTimeFormatter.ofPattern("dd/MM"))
    val description = stringResource(R.string.stats_bar_description, dayFormatted, daily.count)
    val fraction = if (maxCount > 0) {
        (daily.count.toFloat() / maxCount).coerceAtLeast(MinVisibleBarFraction)
    } else {
        MinVisibleBarFraction
    }
    Column(
        modifier = modifier
            .padding(horizontal = SpaceMedium / 4)
            .semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .height(ChartHeight)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(BarWidthFraction)
                    .fillMaxHeight(fraction)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Spacer(Modifier.height(SpaceMedium / 4))
        val locale: Locale = LocalConfiguration.current.locales[0]
        Text(
            text = daily.date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

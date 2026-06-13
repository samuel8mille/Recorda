package com.samuelribeiro.recorda.feature.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.ui.theme.HorizontalPadding
import com.samuelribeiro.recorda.ui.theme.SpaceMedium

/** Long-form reading view of a single [chapter]: its title followed by the full body text. */
@Composable
internal fun ChapterDetail(chapter: Chapter) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HorizontalPadding, vertical = SpaceMedium),
    ) {
        Text(
            modifier = Modifier.semantics { heading() },
            text = chapter.title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.size(SpaceMedium))
        Text(
            text = chapter.body,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

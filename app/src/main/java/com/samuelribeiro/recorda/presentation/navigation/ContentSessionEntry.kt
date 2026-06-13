package com.samuelribeiro.recorda.presentation.navigation

import androidx.compose.runtime.Composable
import com.samuelribeiro.recorda.presentation.ui.content.ContentViewModel

typealias ContentScreenContent = @Composable (ContentViewModel, () -> Unit) -> Unit

/**
 * Bridge between :app (which owns navigation) and :feature:content (which owns the UI).
 *
 * The feature module registers [content] via a ContentProvider that runs at app startup,
 * before any composable is shown. :app references only this object — never the feature module
 * directly — preserving the dynamic-feature dependency direction.
 */
object ContentSessionEntry {
    var content: ContentScreenContent = { _, _ -> }
}

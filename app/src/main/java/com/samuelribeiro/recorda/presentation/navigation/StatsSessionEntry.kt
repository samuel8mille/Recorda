package com.samuelribeiro.recorda.presentation.navigation

import androidx.compose.runtime.Composable
import com.samuelribeiro.recorda.presentation.ui.stats.StatsViewModel

typealias StatsScreenContent = @Composable (StatsViewModel, () -> Unit) -> Unit

/**
 * Bridge between :app (which owns navigation) and :feature:stats (which owns the UI).
 *
 * The feature module registers [content] via a ContentProvider that runs at app startup,
 * before any composable is shown. :app references only this object — never the feature module
 * directly — preserving the dynamic-feature dependency direction.
 */
object StatsSessionEntry {
    var content: StatsScreenContent = { _, _ -> }
}

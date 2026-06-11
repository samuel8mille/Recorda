package com.samuelribeiro.recorda.presentation.navigation

import androidx.compose.runtime.Composable
import com.samuelribeiro.recorda.presentation.ui.study.StudyViewModel

typealias StudyScreenContent = @Composable (StudyViewModel, () -> Unit) -> Unit

/**
 * Bridge between :app (which owns navigation) and :feature:study (which owns the UI).
 *
 * The feature module registers [content] via a ContentProvider that runs at app startup,
 * before any composable is shown. :app references only this object — never the feature module
 * directly — preserving the dynamic-feature dependency direction.
 */
object StudySessionEntry {
    var content: StudyScreenContent = { _, _ -> }
}

package com.samuelribeiro.recorda.core.mvi

import androidx.annotation.StringRes

/** Contract for a screen's UI state. */
interface ScreenUiState {
    @get:StringRes val titleRes: Int
}

package com.samuelribeiro.recorda.core.mvi

import androidx.annotation.StringRes

/** Carries the string resource ID of a loading message. */
data class LoadingUiState(
    @param:StringRes val messageRes: Int,
)

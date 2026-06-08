package com.samuelribeiro.recorda.core.mvi

import androidx.annotation.StringRes

/** Carries the string resource ID of an error message. */
data class ErrorUiState(
    @param:StringRes val messageRes: Int,
)

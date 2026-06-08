package com.samuelribeiro.recorda.presentation.ui.mapper

import android.content.Context
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.network.NetworkError

/**
 * Maps a [Throwable] into a localized, user-readable string.
 *
 * @param context The Android context required to resolve string resources.
 * @return A localized error message.
 */
fun Throwable.asUserMessage(context: Context): String = when (this) {
    is NetworkError.NoInternet    -> context.getString(R.string.error_no_internet)
    is NetworkError.Timeout       -> context.getString(R.string.error_timeout)
    is NetworkError.EmptyResponse -> context.getString(R.string.error_empty_response)
    is NetworkError.HttpError     -> context.getString(R.string.error_http_generic, this.code)
    else                          -> context.getString(R.string.error_unknown)
}

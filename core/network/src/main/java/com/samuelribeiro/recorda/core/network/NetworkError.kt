package com.samuelribeiro.recorda.core.network

/** Sealed hierarchy of network failure types. */
sealed class NetworkError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {

    /** HTTP response with a non-2xx [code]. */
    class HttpError(val code: Int, message: String?) : NetworkError(message)

    /** No network connectivity. */
    class NoInternet(cause: Throwable? = null) : NetworkError(cause = cause)

    /** Successful HTTP response with a null body. */
    class EmptyResponse : NetworkError()

    /** Request exceeded the timeout limit. */
    class Timeout(cause: Throwable? = null) : NetworkError(cause = cause)

    /** Unclassified failure wrapping the original [cause]. */
    class Unknown(cause: Throwable) : NetworkError(cause.message, cause)
}

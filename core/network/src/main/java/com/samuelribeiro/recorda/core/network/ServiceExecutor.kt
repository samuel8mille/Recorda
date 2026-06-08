package com.samuelribeiro.recorda.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

/**
 * Executes network calls as a [Flow]<[Result]<T>> with automatic retry and exponential backoff.
 *
 * HTTP client-agnostic: the [block] must throw [NetworkError] subtypes on failure so that
 * retry logic and error mapping work regardless of the underlying client (Retrofit, Ktor, etc.).
 */
class ServiceExecutor(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    /**
     * Executes [block] and emits its result, retrying on transient failures.
     *
     * @param retries Maximum number of retry attempts before emitting failure.
     * @param isIdempotent Whether the request is safe to retry on timeouts and I/O errors.
     * @param block Suspend lambda that performs the network call and returns [T] directly,
     *              throwing a [NetworkError] subtype on any failure.
     */
    fun <T> execute(
        retries: Int = 2,
        isIdempotent: Boolean = false,
        block: suspend () -> T,
    ): Flow<Result<T>> = flow {
        emit(Result.success(block()))
    }.retryWhen { cause, attempt ->
        if (cause is CancellationException) throw cause
        if (attempt >= retries) return@retryWhen false

        val shouldRetry = when (cause) {
            is ConnectException -> true
            is SocketTimeoutException -> isIdempotent
            is IOException -> isIdempotent
            is NetworkError.HttpError if cause.code in 500..599 -> isIdempotent
            else -> false
        }
        if (shouldRetry) delay(BACKOFF_BASE * 2.0.pow(attempt.toInt()))
        shouldRetry
    }.catch { cause ->
        if (cause is CancellationException) throw cause
        emit(Result.failure(cause.toNetworkError()))
    }.flowOn(ioDispatcher)

    private fun Throwable.toNetworkError(): NetworkError = when (this) {
        is NetworkError -> this
        is SocketTimeoutException -> NetworkError.Timeout(cause = this)
        is IOException -> NetworkError.NoInternet(cause = this)
        else -> NetworkError.Unknown(this)
    }

    private companion object {
        val BACKOFF_BASE = 500.milliseconds
    }
}

package com.machinarium.notesv2.core.network

import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.common.result.AppResult
import java.io.IOException
import javax.net.ssl.SSLPeerUnverifiedException
import kotlinx.coroutines.delay
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

private const val DEFAULT_MAX_ATTEMPTS = 3
private const val INITIAL_BACKOFF_MILLIS = 500L
private const val MAX_BACKOFF_MILLIS = 4_000L
private const val BACKOFF_FACTOR = 2
private const val FIRST_SERVER_ERROR_CODE = 500

/**
 * Runs an API call and maps failures to [AppError] (ERR-01). Transient failures (network, 5xx) are retried
 * with exponential backoff (DATA-03); client errors and parse errors fail immediately.
 * CancellationException is never caught, so structured concurrency keeps working.
 */
suspend fun <T> safeApiCall(
    maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
    initialBackoffMillis: Long = INITIAL_BACKOFF_MILLIS,
    block: suspend () -> T,
): AppResult<T> {
    var backoff = initialBackoffMillis
    repeat(maxAttempts - 1) {
        val result = attempt(block)
        if (result is AppResult.Success || !(result as AppResult.Failure).error.isTransient()) return result
        delay(backoff)
        backoff = (backoff * BACKOFF_FACTOR).coerceAtMost(MAX_BACKOFF_MILLIS)
    }
    return attempt(block)
}

private suspend fun <T> attempt(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (exception: SSLPeerUnverifiedException) {
    // ERR-08: certificate pin mismatch — not a connectivity problem, and retrying can't fix it. Must precede IOException.
    AppResult.Failure(AppError.Unknown)
} catch (exception: IOException) {
    AppResult.Failure(AppError.Network)
} catch (exception: HttpException) {
    AppResult.Failure(AppError.Server(exception.code()))
} catch (exception: SerializationException) {
    AppResult.Failure(AppError.Unknown)
}

private fun AppError.isTransient(): Boolean =
    this == AppError.Network || (this is AppError.Server && code >= FIRST_SERVER_ERROR_CODE)

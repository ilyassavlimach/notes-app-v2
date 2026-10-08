package com.machinarium.notesv2.core.common.result

/** Result of an operation that can fail with a typed [AppError]. */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}

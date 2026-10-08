package com.machinarium.notesv2.core.common.result

/** Typed failure shared by every layer (ERR-01). The UI maps it to a message via core:ui `messageRes()`. */
sealed interface AppError {
    /** No connectivity or the request timed out. */
    data object Network : AppError

    /** The server answered with an HTTP error status. */
    data class Server(val code: Int) : AppError

    /** Anything unexpected (parsing, programming errors surfaced as data). */
    data object Unknown : AppError
}

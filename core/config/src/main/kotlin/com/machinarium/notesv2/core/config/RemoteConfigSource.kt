package com.machinarium.notesv2.core.config

/** The few calls the repository needs from the remote-config SDK, so its logic is testable with a fake. */
interface RemoteConfigSource {
    /** Throws the SDK's exceptions (e.g. FirebaseNetworkException) on failure. */
    suspend fun fetchAndActivate()

    fun getLong(key: String): Long

    fun getBoolean(key: String): Boolean
}

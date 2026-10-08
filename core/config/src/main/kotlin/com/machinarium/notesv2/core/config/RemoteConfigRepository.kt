package com.machinarium.notesv2.core.config

import com.machinarium.notesv2.core.common.result.AppResult
import kotlinx.coroutines.flow.StateFlow

interface RemoteConfigRepository {
    /** Starts with the in-app defaults, so the app works before (or without) the first fetch. */
    val appGate: StateFlow<AppGate>

    suspend fun refresh(): AppResult<Unit>
}

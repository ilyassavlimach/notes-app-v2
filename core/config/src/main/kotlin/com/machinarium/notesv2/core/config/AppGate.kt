package com.machinarium.notesv2.core.config

/** Remote values that can block the app (RC-01). */
data class AppGate(val minVersionCode: Long, val isMaintenance: Boolean)

sealed interface AppGateState {
    data object Open : AppGateState

    data object UpdateRequired : AppGateState

    data object Maintenance : AppGateState
}

/** Maintenance wins over a required update: there is nothing to update to while the backend is down. */
fun AppGate.evaluate(currentVersionCode: Long): AppGateState = when {
    isMaintenance -> AppGateState.Maintenance
    currentVersionCode < minVersionCode -> AppGateState.UpdateRequired
    else -> AppGateState.Open
}

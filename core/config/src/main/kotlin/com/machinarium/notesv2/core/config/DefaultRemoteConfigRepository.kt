package com.machinarium.notesv2.core.config

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.common.result.AppResult
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class DefaultRemoteConfigRepository @Inject constructor(private val source: RemoteConfigSource) :
    RemoteConfigRepository {

    private val gate = MutableStateFlow(DEFAULT_GATE)
    override val appGate: StateFlow<AppGate> = gate.asStateFlow()

    override suspend fun refresh(): AppResult<Unit> = try {
        source.fetchAndActivate()
        gate.value = AppGate(
            minVersionCode = source.getLong(KEY_MIN_VERSION_CODE),
            isMaintenance = source.getBoolean(KEY_MAINTENANCE),
        )
        AppResult.Success(Unit)
    } catch (exception: FirebaseNetworkException) {
        // The last activated (or default) values stay in place.
        AppResult.Failure(AppError.Network)
    } catch (exception: FirebaseException) {
        AppResult.Failure(AppError.Unknown)
    }

    internal companion object {
        const val KEY_MIN_VERSION_CODE = "min_version_code"
        const val KEY_MAINTENANCE = "maintenance_mode"
        val DEFAULT_GATE = AppGate(minVersionCode = 0, isMaintenance = false)
        val DEFAULTS: Map<String, Any> = mapOf(
            KEY_MIN_VERSION_CODE to DEFAULT_GATE.minVersionCode,
            KEY_MAINTENANCE to DEFAULT_GATE.isMaintenance,
        )
    }
}

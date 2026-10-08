package com.machinarium.notesv2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machinarium.notesv2.BuildConfig
import com.machinarium.notesv2.core.config.AppGateState
import com.machinarium.notesv2.core.config.RemoteConfigRepository
import com.machinarium.notesv2.core.config.evaluate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** RC-01: decides whether the app opens, asks for an update, or shows maintenance. */
@HiltViewModel
internal class AppGateViewModel(remoteConfigRepository: RemoteConfigRepository, versionCode: Long) : ViewModel() {

    @Inject
    constructor(remoteConfigRepository: RemoteConfigRepository) : this(
        remoteConfigRepository,
        BuildConfig.VERSION_CODE.toLong(),
    )

    val state: StateFlow<AppGateState> = remoteConfigRepository.appGate
        .map { gate -> gate.evaluate(versionCode) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AppGateState.Open, // the in-app defaults never block, so start open
        )

    init {
        // A failed fetch keeps the last (or default) values, so the result needs no handling here.
        viewModelScope.launch { remoteConfigRepository.refresh() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

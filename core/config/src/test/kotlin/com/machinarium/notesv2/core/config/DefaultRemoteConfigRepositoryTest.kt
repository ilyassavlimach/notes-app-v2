package com.machinarium.notesv2.core.config

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigClientException
import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.common.result.AppResult
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultRemoteConfigRepositoryTest {

    private val source = FakeRemoteConfigSource()
    private val repository = DefaultRemoteConfigRepository(source)

    @Test
    fun `before any fetch, then the gate uses the in-app defaults`() {
        assertEquals(DefaultRemoteConfigRepository.DEFAULT_GATE, repository.appGate.value)
    }

    @Test
    fun `given fetched values, when refreshed, then the gate reflects them`() = runTest {
        source.longs[DefaultRemoteConfigRepository.KEY_MIN_VERSION_CODE] = 42
        source.booleans[DefaultRemoteConfigRepository.KEY_MAINTENANCE] = true

        val result = repository.refresh()

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(AppGate(minVersionCode = 42, isMaintenance = true), repository.appGate.value)
    }

    @Test
    fun `given no network, when refreshed, then fails with network error and keeps the gate`() = runTest {
        source.failure = FirebaseNetworkException("offline")

        assertEquals(AppResult.Failure(AppError.Network), repository.refresh())
        assertEquals(DefaultRemoteConfigRepository.DEFAULT_GATE, repository.appGate.value)
    }

    @Test
    fun `given another sdk failure, when refreshed, then fails with unknown error`() = runTest {
        source.failure = FirebaseRemoteConfigClientException("throttled")

        assertEquals(AppResult.Failure(AppError.Unknown), repository.refresh())
    }

    private class FakeRemoteConfigSource : RemoteConfigSource {
        val longs = mutableMapOf<String, Long>()
        val booleans = mutableMapOf<String, Boolean>()
        var failure: Exception? = null

        override suspend fun fetchAndActivate() {
            failure?.let { throw it }
        }

        override fun getLong(key: String): Long = longs[key] ?: 0

        override fun getBoolean(key: String): Boolean = booleans[key] ?: false
    }
}

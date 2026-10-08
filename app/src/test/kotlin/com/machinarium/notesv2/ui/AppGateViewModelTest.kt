package com.machinarium.notesv2.ui

import com.machinarium.notesv2.core.common.result.AppResult
import com.machinarium.notesv2.core.config.AppGate
import com.machinarium.notesv2.core.config.AppGateState
import com.machinarium.notesv2.core.config.RemoteConfigRepository
import com.machinarium.notesv2.core.testing.MainDispatcherRule
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppGateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRemoteConfigRepository()

    private fun TestScope.createViewModel(): AppGateViewModel {
        val viewModel = AppGateViewModel(repository, versionCode = CURRENT_VERSION)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        return viewModel
    }

    @Test
    fun `when created, then refreshes the config once and is open by default`() = runTest {
        val viewModel = createViewModel()

        assertEquals(AppGateState.Open, viewModel.state.value)
        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun `given a newer minimum version, then an update is required`() = runTest {
        val viewModel = createViewModel()

        repository.appGate.value = AppGate(minVersionCode = CURRENT_VERSION + 1, isMaintenance = false)

        assertEquals(AppGateState.UpdateRequired, viewModel.state.value)
    }

    @Test
    fun `given maintenance mode, then shows maintenance`() = runTest {
        val viewModel = createViewModel()

        repository.appGate.value = AppGate(minVersionCode = 0, isMaintenance = true)

        assertEquals(AppGateState.Maintenance, viewModel.state.value)
    }

    private class FakeRemoteConfigRepository : RemoteConfigRepository {
        override val appGate = MutableStateFlow(AppGate(minVersionCode = 0, isMaintenance = false))
        var refreshCount = 0
            private set

        override suspend fun refresh(): AppResult<Unit> {
            refreshCount++
            return AppResult.Success(Unit)
        }
    }

    private companion object {
        const val CURRENT_VERSION = 5L
    }
}

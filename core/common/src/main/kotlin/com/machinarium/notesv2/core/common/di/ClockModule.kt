package com.machinarium.notesv2.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

/** The time source, injected so tests can use a fixed clock instead of the real time. */
@Module
@InstallIn(SingletonComponent::class)
internal object ClockModule {
    @Provides
    fun provideClock(): Clock = Clock.systemUTC()
}

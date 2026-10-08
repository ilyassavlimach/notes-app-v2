package com.machinarium.notesv2.di

import com.machinarium.notesv2.BuildConfig
import com.machinarium.notesv2.core.common.config.ApiConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Hands the current flavor's API values to :core:network (ENV-01). */
@Module
@InstallIn(SingletonComponent::class)
internal object ApiConfigModule {
    @Provides
    fun provideApiConfig(): ApiConfig = ApiConfig.from(BuildConfig.API_BASE_URL, BuildConfig.API_CERT_PINS)
}

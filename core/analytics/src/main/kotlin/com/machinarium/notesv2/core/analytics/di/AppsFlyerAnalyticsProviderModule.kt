package com.machinarium.notesv2.core.analytics.di

import com.machinarium.notesv2.core.analytics.AnalyticsProvider
import com.machinarium.notesv2.core.analytics.providers.AppsFlyerAnalyticsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal interface AppsFlyerAnalyticsProviderModule {
    @Binds
    @IntoSet
    fun bindAppsFlyerAnalyticsProvider(impl: AppsFlyerAnalyticsProvider): AnalyticsProvider
}

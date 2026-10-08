package com.machinarium.notesv2.core.analytics.di

import com.machinarium.notesv2.core.analytics.AnalyticsTracker
import com.machinarium.notesv2.core.analytics.CompositeAnalyticsTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Providers join the composite's set with @Binds @IntoSet (one module per provider template). :core:analytics is
 * only created when at least one provider is chosen, so the set is never empty.
 */
@Module
@InstallIn(SingletonComponent::class)
internal interface AnalyticsModule {
    @Binds
    fun bindAnalyticsTracker(impl: CompositeAnalyticsTracker): AnalyticsTracker
}

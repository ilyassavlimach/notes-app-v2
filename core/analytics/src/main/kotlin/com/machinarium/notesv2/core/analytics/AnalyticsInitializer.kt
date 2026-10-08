package com.machinarium.notesv2.core.analytics

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/** SDK-01: starts every analytics provider once, before the first screen (attribution SDKs need the launch). */
class AnalyticsInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        EntryPointAccessors.fromApplication(context, AnalyticsEntryPoint::class.java)
            .compositeAnalyticsTracker()
            .initializeAll()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface AnalyticsEntryPoint {
        fun compositeAnalyticsTracker(): CompositeAnalyticsTracker
    }
}

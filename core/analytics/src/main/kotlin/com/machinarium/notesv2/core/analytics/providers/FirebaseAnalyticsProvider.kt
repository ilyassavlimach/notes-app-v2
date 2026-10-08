package com.machinarium.notesv2.core.analytics.providers

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.machinarium.notesv2.core.analytics.AnalyticsEvent
import com.machinarium.notesv2.core.analytics.AnalyticsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Firebase Analytics (configured by google-services.json; no key here). */
@Singleton
internal class FirebaseAnalyticsProvider @Inject constructor(@ApplicationContext private val context: Context) :
    AnalyticsProvider {
    override val name = "firebase"

    private val analytics by lazy { FirebaseAnalytics.getInstance(context) }

    override fun track(event: AnalyticsEvent) {
        analytics.logEvent(
            event.name,
            Bundle().apply {
                event.params.forEach { (key, value) -> putString(key, value) }
            },
        )
    }

    override fun setUserId(userId: String?) = analytics.setUserId(userId)

    override fun setEnabled(enabled: Boolean) = analytics.setAnalyticsCollectionEnabled(enabled)
}

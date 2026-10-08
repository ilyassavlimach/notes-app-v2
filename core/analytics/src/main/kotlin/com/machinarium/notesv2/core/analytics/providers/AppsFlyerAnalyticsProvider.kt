package com.machinarium.notesv2.core.analytics.providers

import android.content.Context
import com.appsflyer.AppsFlyerLib
import com.machinarium.notesv2.core.analytics.AnalyticsEvent
import com.machinarium.notesv2.core.analytics.AnalyticsProvider
import com.machinarium.notesv2.core.analytics.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/** AppsFlyer attribution + events; dev key from `analytics.appsflyer.devKey`. */
@Singleton
internal class AppsFlyerAnalyticsProvider @Inject constructor(@ApplicationContext private val context: Context) :
    AnalyticsProvider {
    override val name = "appsflyer"

    private val isConfigured = BuildConfig.APPSFLYER_DEV_KEY.isNotBlank()
    private val appsFlyer get() = AppsFlyerLib.getInstance()

    override fun initialize() {
        if (!isConfigured) {
            Timber.w("AppsFlyer dev key missing (analytics.appsflyer.devKey); AppsFlyer stays off")
            return
        }
        appsFlyer.init(BuildConfig.APPSFLYER_DEV_KEY, null, context)
        appsFlyer.start()
    }

    override fun map(event: AnalyticsEvent): AnalyticsEvent? = event.takeIf { isConfigured }

    override fun track(event: AnalyticsEvent) = appsFlyer.logEvent(context, event.name, event.params)

    override fun setUserId(userId: String?) = appsFlyer.setCustomerUserId(userId)

    override fun setEnabled(enabled: Boolean) = appsFlyer.stop(!enabled, context)
}

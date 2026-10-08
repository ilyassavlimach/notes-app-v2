package com.machinarium.notesv2.core.analytics.providers

import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.machinarium.notesv2.core.analytics.AnalyticsEvent
import com.machinarium.notesv2.core.analytics.AnalyticsProvider
import com.machinarium.notesv2.core.analytics.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * Adjust tracks events by token, not by name (OBS-05 mapping): only events listed in `analytics.adjust.eventTokens`
 * (`note_opened:abc123,purchase:def456`) are sent. Sandbox in debug builds, production in release.
 */
@Singleton
internal class AdjustAnalyticsProvider @Inject constructor(@ApplicationContext private val context: Context) :
    AnalyticsProvider {
    override val name = "adjust"

    private val eventTokens = parseEventTokens(BuildConfig.ADJUST_EVENT_TOKENS)
    private val isConfigured = BuildConfig.ADJUST_APP_TOKEN.isNotBlank()

    override fun initialize() {
        if (!isConfigured) {
            Timber.w("Adjust app token missing (analytics.adjust.appToken); Adjust stays off")
            return
        }
        val environment =
            if (BuildConfig.DEBUG) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION
        Adjust.initSdk(AdjustConfig(context, BuildConfig.ADJUST_APP_TOKEN, environment))
    }

    override fun map(event: AnalyticsEvent): AnalyticsEvent? = event.takeIf { isConfigured && it.name in eventTokens }

    override fun track(event: AnalyticsEvent) {
        val token = eventTokens[event.name] ?: return
        Adjust.trackEvent(
            AdjustEvent(token).apply {
                event.params.forEach { (key, value) -> addCallbackParameter(key, value) }
            },
        )
    }

    override fun setUserId(userId: String?) {
        if (userId ==
            null
        ) {
            Adjust.removeGlobalCallbackParameter(USER_ID_KEY)
        } else {
            Adjust.addGlobalCallbackParameter(USER_ID_KEY, userId)
        }
    }

    override fun setEnabled(enabled: Boolean) = if (enabled) Adjust.enable() else Adjust.disable()

    private companion object {
        const val USER_ID_KEY = "user_id"
    }
}

/** `name:token,name2:token2` → map; malformed or blank entries are ignored. */
internal fun parseEventTokens(csv: String): Map<String, String> = csv.split(',')
    .mapNotNull { entry ->
        val parts = entry.split(':').map(String::trim)
        if (parts.size == 2 && parts.none(String::isEmpty)) parts[0] to parts[1] else null
    }
    .toMap()

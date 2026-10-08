package com.machinarium.notesv2.core.analytics

/**
 * One analytics SDK behind the composite (OBS-05). Implementations live in this module, one file per provider,
 * and are added to the set with `@Binds @IntoSet`.
 */
interface AnalyticsProvider {
    /** Short name used in logs when this provider fails. */
    val name: String

    /** Starts the SDK; called once at app start by [AnalyticsInitializer] (SDK-01). */
    fun initialize() {}

    /** Provider-specific mapping: rename the event or return `null` to skip it for this provider only. */
    fun map(event: AnalyticsEvent): AnalyticsEvent? = event

    fun track(event: AnalyticsEvent)

    fun setUserId(userId: String?)

    fun setEnabled(enabled: Boolean)
}

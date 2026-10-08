package com.machinarium.notesv2.core.analytics

/** What ViewModels use to record events (OBS-03). Tests use FakeAnalyticsTracker from :core:testing. */
interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)

    /** Pseudonymous id only (never e-mail or phone); `null` on logout. */
    fun setUserId(userId: String?)

    /** Consent switch for every provider at once (OBS-05). */
    fun setEnabled(enabled: Boolean)
}

package com.machinarium.notesv2.core.testing.analytics

import com.machinarium.notesv2.core.analytics.AnalyticsEvent
import com.machinarium.notesv2.core.analytics.AnalyticsTracker

/** Records events so ViewModel tests can assert what was tracked. Needs `api(projects.core.analytics)` in :core:testing. */
class FakeAnalyticsTracker : AnalyticsTracker {
    private val recorded = mutableListOf<AnalyticsEvent>()

    val events: List<AnalyticsEvent> get() = recorded.toList()

    var userId: String? = null
        private set

    var isEnabled: Boolean = true
        private set

    override fun track(event: AnalyticsEvent) {
        recorded += event
    }

    override fun setUserId(userId: String?) {
        this.userId = userId
    }

    override fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
}

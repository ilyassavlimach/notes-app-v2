package com.machinarium.notesv2.core.analytics

/**
 * One analytics event. Names and parameter keys are constants owned by the feature that logs them; values must
 * never contain personal data (OBS-03, SEC-06). The limits match Firebase Analytics so bad events fail in tests,
 * not silently in production.
 */
data class AnalyticsEvent(val name: String, val params: Map<String, String> = emptyMap()) {
    init {
        require(NAME_PATTERN.matches(name)) { "Invalid event name '$name'" }
        require(params.size <= MAX_PARAMS) { "Too many params (${params.size}) for '$name'" }
        params.keys.forEach { key -> require(NAME_PATTERN.matches(key)) { "Invalid param key '$key' in '$name'" } }
    }

    private companion object {
        const val MAX_PARAMS = 25
        val NAME_PATTERN = Regex("^[a-zA-Z][a-zA-Z0-9_]{0,39}$")
    }
}

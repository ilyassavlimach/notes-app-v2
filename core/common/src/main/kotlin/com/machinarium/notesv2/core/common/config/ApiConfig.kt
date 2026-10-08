package com.machinarium.notesv2.core.common.config

/**
 * Per-environment API settings (ENV-01). :app provides it from its flavor's BuildConfig, so library modules never
 * read environment-specific values themselves.
 */
data class ApiConfig(
    val baseUrl: String,
    /** `sha256/…` pins for [baseUrl]'s host; empty = pinning off (HARD-01). */
    val certPins: List<String>,
) {
    companion object {
        /** Builds the config from Gradle-property strings: pins are comma-separated, blanks ignored. */
        fun from(
            baseUrl: String,
            certPinsCsv: String,
        ): ApiConfig = ApiConfig(
            baseUrl = baseUrl,
            certPins = certPinsCsv.split(',').map(String::trim).filter(String::isNotEmpty),
        )
    }
}

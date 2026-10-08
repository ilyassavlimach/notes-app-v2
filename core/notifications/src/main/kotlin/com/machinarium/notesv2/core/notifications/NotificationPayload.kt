package com.machinarium.notesv2.core.notifications

/**
 * What a push message is allowed to show and open (PUSH-02). The deep link is kept only when it uses one of
 * [allowedSchemes]; navigation then matches it against the declared deep-link routes, so unknown paths land on
 * the start destination instead of an arbitrary screen.
 */
data class NotificationPayload(val title: String, val body: String, val deepLink: String?) {
    companion object {
        const val KEY_DEEP_LINK = "deeplink"

        fun from(
            title: String?,
            body: String?,
            data: Map<String, String>,
            allowedSchemes: Set<String>,
        ): NotificationPayload? {
            if (title.isNullOrBlank() && body.isNullOrBlank()) return null
            val link = data[KEY_DEEP_LINK]?.takeIf { candidate ->
                allowedSchemes.any { scheme -> candidate.startsWith("$scheme://") }
            }
            return NotificationPayload(title = title.orEmpty(), body = body.orEmpty(), deepLink = link)
        }
    }
}

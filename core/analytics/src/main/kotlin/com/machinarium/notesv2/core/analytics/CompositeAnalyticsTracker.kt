package com.machinarium.notesv2.core.analytics

import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * Sends every event to all chosen providers (OBS-03/05). Consent and user id are set once here and forwarded;
 * a provider that throws is logged and skipped, so one broken SDK never breaks the others.
 * Starts enabled — when the spec requires opt-in consent, call `setEnabled(false)` at startup until the user agrees.
 */
@Singleton
internal class CompositeAnalyticsTracker @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards AnalyticsProvider>,
) : AnalyticsTracker {

    @Volatile
    private var isEnabled = true

    fun initializeAll() = forEachProvider { it.initialize() }

    override fun track(event: AnalyticsEvent) {
        if (!isEnabled) return
        forEachProvider { provider -> provider.map(event)?.let(provider::track) }
    }

    override fun setUserId(userId: String?) = forEachProvider { it.setUserId(userId) }

    override fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        forEachProvider { it.setEnabled(enabled) }
    }

    // Third-party SDKs may throw anything; each provider is isolated (OBS-05).
    @Suppress("TooGenericExceptionCaught") // justified: SDK failures must not reach callers or other providers
    private inline fun forEachProvider(action: (AnalyticsProvider) -> Unit) {
        providers.forEach { provider ->
            try {
                action(provider)
            } catch (exception: RuntimeException) {
                Timber.w(exception, "Analytics provider %s failed", provider.name)
            }
        }
    }
}

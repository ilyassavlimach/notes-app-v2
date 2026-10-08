package com.machinarium.notesv2.core.notifications

/**
 * Receives new FCM tokens (PUSH-03). Implement it in :core:data (e.g. a repository that registers the token with
 * the backend through WorkManager) and bind it with @Binds; until then tokens are simply not forwarded.
 * Must be idempotent: the same token can arrive more than once.
 */
fun interface PushTokenSink {
    fun onNewToken(token: String)
}

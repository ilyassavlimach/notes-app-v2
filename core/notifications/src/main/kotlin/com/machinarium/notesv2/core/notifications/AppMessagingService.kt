package com.machinarium.notesv2.core.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import java.util.Optional
import javax.inject.Inject

@AndroidEntryPoint
class AppMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushTokenSink: Optional<PushTokenSink>

    // PUSH-03: no network work here — the sink decides how the token reaches the backend.
    // firebase-messaging 26+ reports tokens through onRegistered; older delivery paths still call onNewToken,
    // so both forward to the same (idempotent) sink.
    override fun onRegistered(token: String) {
        pushTokenSink.ifPresent { it.onNewToken(token) }
    }

    @Deprecated("Superseded by onRegistered in firebase-messaging 26; kept for the legacy NEW_TOKEN delivery.")
    override fun onNewToken(token: String) {
        onRegistered(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val payload = NotificationPayload.from(
            title = message.notification?.title,
            body = message.notification?.body,
            data = message.data,
            allowedSchemes = ALLOWED_DEEP_LINK_SCHEMES,
        ) ?: return
        // PUSH-01: without the permission (API 33+) the notification is dropped, never forced.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notification = NotificationCompat.Builder(this, AppNotificationChannel.General.id)
            .setSmallIcon(applicationInfo.icon)
            .setContentTitle(payload.title)
            .setContentText(payload.body)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(payload))
            .build()
        NotificationManagerCompat.from(this).notify(message.messageId.hashCode(), notification)
    }

    /** PUSH-02: immutable PendingIntent; a deep link opens through the app's own navigation deep links. */
    private fun contentIntent(payload: NotificationPayload): PendingIntent {
        val intent = payload.deepLink
            ?.let { Intent(Intent.ACTION_VIEW, it.toUri()).setPackage(packageName) }
            ?: requireNotNull(packageManager.getLaunchIntentForPackage(packageName)) { "App has no launcher activity" }
        return PendingIntent.getActivity(
            this,
            payload.hashCode(),
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private companion object {
        // SPEC.md: https App Links only; MainActivity's DeepLinkParser then checks the host and path (PUSH-02).
        val ALLOWED_DEEP_LINK_SCHEMES = setOf("https")
    }
}

package com.machinarium.notesv2.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.startup.Initializer

/** Creates the app's channels at startup (PUSH-01, SDK-01). Creating an existing channel again is a no-op. */
class NotificationChannelsInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        AppNotificationChannel.entries.forEach { channel ->
            manager.createNotificationChannel(
                NotificationChannel(channel.id, context.getString(channel.nameRes), channel.importance).apply {
                    description = context.getString(channel.descriptionRes)
                },
            )
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

package com.machinarium.notesv2.core.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationChannelsInitializerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val manager = context.getSystemService(NotificationManager::class.java)

    @Test
    fun `when created, then every app channel exists with its translated name`() {
        NotificationChannelsInitializer().create(context)

        AppNotificationChannel.entries.forEach { channel ->
            val created = assertNotNull(manager.getNotificationChannel(channel.id))
            assertEquals(context.getString(channel.nameRes), created.name)
            assertEquals(channel.importance, created.importance)
        }
    }

    @Test
    fun `when created twice, then channels are not duplicated`() {
        repeat(2) { NotificationChannelsInitializer().create(context) }

        assertEquals(AppNotificationChannel.entries.size, manager.notificationChannels.size)
    }
}

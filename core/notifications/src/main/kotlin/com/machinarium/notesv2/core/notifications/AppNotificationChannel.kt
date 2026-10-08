package com.machinarium.notesv2.core.notifications

import android.app.NotificationManager
import androidx.annotation.StringRes
import com.machinarium.notesv2.core.i18n.R

/** Every channel the app posts to (PUSH-01). Add a value here — never create channels ad hoc. */
enum class AppNotificationChannel(
    val id: String,
    @param:StringRes val nameRes: Int,
    @param:StringRes val descriptionRes: Int,
    val importance: Int,
) {
    General(
        id = "general",
        nameRes = R.string.notifications_channel_general_name,
        descriptionRes = R.string.notifications_channel_general_description,
        importance = NotificationManager.IMPORTANCE_DEFAULT,
    ),
}

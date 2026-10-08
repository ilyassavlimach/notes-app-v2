package com.machinarium.notesv2.core.notifications.di

import com.machinarium.notesv2.core.notifications.PushTokenSink
import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface NotificationsModule {
    /** Optional until :core:data binds a real sink (PUSH-03). */
    @BindsOptionalOf
    fun optionalPushTokenSink(): PushTokenSink
}

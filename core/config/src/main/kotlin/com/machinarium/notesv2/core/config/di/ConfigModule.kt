package com.machinarium.notesv2.core.config.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.machinarium.notesv2.core.config.DefaultRemoteConfigRepository
import com.machinarium.notesv2.core.config.RemoteConfigRepository
import com.machinarium.notesv2.core.config.RemoteConfigSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Module
@InstallIn(SingletonComponent::class)
internal interface ConfigModule {
    @Binds
    @Singleton
    fun bindRemoteConfigRepository(impl: DefaultRemoteConfigRepository): RemoteConfigRepository

    companion object {
        private const val MIN_FETCH_INTERVAL_SECONDS = 3_600L

        /** RC-01: in-app defaults + a minimum fetch interval; the SDK is wrapped so the repository stays testable. */
        @Provides
        @Singleton
        fun provideRemoteConfigSource(): RemoteConfigSource {
            val remoteConfig = FirebaseRemoteConfig.getInstance().apply {
                setConfigSettingsAsync(
                    remoteConfigSettings {
                        minimumFetchIntervalInSeconds = MIN_FETCH_INTERVAL_SECONDS
                    },
                )
                setDefaultsAsync(DefaultRemoteConfigRepository.DEFAULTS)
            }
            return object : RemoteConfigSource {
                override suspend fun fetchAndActivate() {
                    remoteConfig.fetchAndActivate().await()
                }

                override fun getLong(key: String): Long = remoteConfig.getLong(key)

                override fun getBoolean(key: String): Boolean = remoteConfig.getBoolean(key)
            }
        }
    }
}

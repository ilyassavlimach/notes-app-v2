package com.machinarium.notesv2.core.network.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.machinarium.notesv2.core.common.config.ApiConfig
import com.machinarium.notesv2.core.network.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    private const val TIMEOUT_SECONDS = 20L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        apiConfig: ApiConfig,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .apply {
            buildCertificatePinner(apiConfig)?.let { certificatePinner(it) }
            if (BuildConfig.DEBUG) {
                // OBS-02: in release the no-op artifact makes this interceptor a pass-through.
                addInterceptor(ChuckerInterceptor.Builder(context).build())
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                        redactHeader("Authorization")
                        redactHeader("Cookie")
                    },
                )
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        json: Json,
        apiConfig: ApiConfig,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(apiConfig.baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    /** HARD-01: pins come from the flavor's `api.<env>.certPins` property; empty means pinning is off. */
    private fun buildCertificatePinner(apiConfig: ApiConfig): CertificatePinner? {
        if (apiConfig.certPins.isEmpty()) return null
        val host = apiConfig.baseUrl.toHttpUrl().host
        return CertificatePinner.Builder()
            .apply { apiConfig.certPins.forEach { add(host, it) } }
            .build()
    }
}

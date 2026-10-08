package com.machinarium.notesv2

import android.app.Application
import android.os.StrictMode
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class NotesV2Application : Application() {
    override fun onCreate() {
        super.onCreate()
        // OBS-01: crashes from debug builds would only add noise to the Crashlytics dashboard.
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            // PERF-01: surface disk/network access on the main thread during development.
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().penaltyLog().build())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }
}

/** Release logging: warnings and errors go to Crashlytics as breadcrumbs, exceptions as non-fatals (OBS-01). */
private class CrashReportingTree : Timber.Tree() {
    override fun isLoggable(
        tag: String?,
        priority: Int,
    ): Boolean = priority >= android.util.Log.WARN // the priority constant only; logging goes through Timber

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log(message)
        t?.let(crashlytics::recordException)
    }
}

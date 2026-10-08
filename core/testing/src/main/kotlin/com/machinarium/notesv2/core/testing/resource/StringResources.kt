package com.machinarium.notesv2.core.testing.resource

import android.content.Context
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider

/** Resolves a string resource in Robolectric UI tests (TEST-09), so assertions find texts the way users see them. */
fun stringResource(@StringRes id: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(id)

package com.machinarium.notesv2.core.common.dispatcher

import javax.inject.Qualifier

/** Inject this instead of referencing Dispatchers.IO directly (CONC-01), so tests can swap it. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

package com.machinarium.notesv2.core.model

/** A note as the app understands it, independent of how it is fetched or stored. */
data class Note(val id: Long, val title: String, val body: String)

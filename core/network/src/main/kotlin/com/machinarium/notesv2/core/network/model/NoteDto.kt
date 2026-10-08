package com.machinarium.notesv2.core.network.model

import kotlinx.serialization.Serializable

/** Wire format of `GET /posts`. `userId` is part of the payload but the app has no use for it. */
@Serializable
data class NoteDto(val id: Long, val title: String, val body: String)

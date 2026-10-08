package com.machinarium.notesv2.core.network

import com.machinarium.notesv2.core.network.model.NoteDto
import retrofit2.http.GET

interface NotesApi {
    @GET("posts")
    suspend fun getNotes(): List<NoteDto>
}

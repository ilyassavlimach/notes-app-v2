package com.machinarium.notesv2.core.data.di

import com.machinarium.notesv2.core.data.repository.NotesRepository
import com.machinarium.notesv2.core.data.repository.OfflineFirstNotesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {
    @Binds
    fun bindNotesRepository(impl: OfflineFirstNotesRepository): NotesRepository
}

package com.machinarium.notesv2.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.machinarium.notesv2.core.database.dao.NoteDao
import com.machinarium.notesv2.core.database.model.NoteEntity

@Database(entities = [NoteEntity::class], version = 1, exportSchema = true)
internal abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}

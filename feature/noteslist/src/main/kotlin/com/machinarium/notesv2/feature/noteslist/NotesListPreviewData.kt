package com.machinarium.notesv2.feature.noteslist

import kotlinx.collections.immutable.persistentListOf

/** Sample data shared by previews and UI tests. */
internal val PreviewNotes = persistentListOf(
    NoteItemUi(
        id = 1,
        title = "Weekly groceries",
        preview = "Milk, eggs, bread, coffee beans and something for the weekend dinner with friends.",
    ),
    NoteItemUi(
        id = 2,
        title = "Book club",
        preview = "Finish chapters four to six and write down two questions for Thursday.",
    ),
)

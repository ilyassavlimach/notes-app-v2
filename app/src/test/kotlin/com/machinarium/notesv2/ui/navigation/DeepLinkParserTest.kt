package com.machinarium.notesv2.ui.navigation

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.machinarium.notesv2.feature.notedetail.navigation.NoteDetailKey
import com.machinarium.notesv2.feature.noteslist.navigation.NotesListKey
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinkParserTest {

    private val parser = DeepLinkParser(host = HOST)

    @Test
    fun `given a note link, then opens the list and that note`() {
        assertEquals(listOf(NotesListKey, NoteDetailKey(7)), parser.parse(Uri.parse("https://$HOST/notes/7")))
    }

    @Test
    fun `given no link, then opens the list`() {
        assertEquals(listOf(NotesListKey), parser.parse(null))
    }

    @Test
    fun `given invalid links, then each opens the list`() {
        listOf(
            "https://other.example.com/notes/7",
            "http://$HOST/notes/7",
            "https://$HOST/notes/abc",
            "https://$HOST/notes/0",
            "https://$HOST/notes/-3",
            "https://$HOST/notes",
            "https://$HOST/notes/7/edit",
            "https://$HOST/users/7",
        ).forEach { link ->
            assertEquals(listOf(NotesListKey), parser.parse(Uri.parse(link)), link)
        }
    }

    private companion object {
        const val HOST = "notes.example.com"
    }
}

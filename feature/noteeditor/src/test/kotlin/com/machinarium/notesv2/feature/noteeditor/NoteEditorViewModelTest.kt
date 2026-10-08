package com.machinarium.notesv2.feature.noteeditor

import androidx.lifecycle.SavedStateHandle
import com.machinarium.notesv2.core.analytics.AnalyticsEvent
import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.common.result.AppResult
import com.machinarium.notesv2.core.model.Note
import com.machinarium.notesv2.core.testing.MainDispatcherRule
import com.machinarium.notesv2.core.testing.analytics.FakeAnalyticsTracker
import com.machinarium.notesv2.core.testing.repository.FakeNotesRepository
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNotesRepository()
    private val analytics = FakeAnalyticsTracker()

    private fun TestScope.createViewModel(
        noteId: Long? = null,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ): NoteEditorViewModel {
        val viewModel = NoteEditorViewModel(noteId, savedStateHandle, repository, analytics)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    private fun NoteEditorViewModel.editing() = assertIs<NoteEditorUiState.Editing>(uiState.value)

    @Test
    fun `given a new note, then starts empty and can't be saved yet`() = runTest {
        val state = createViewModel().editing()

        assertEquals(true to "", state.isNewNote to state.title)
        assertEquals(false, state.canSave)
        assertNull(state.titleError)
    }

    @Test
    fun `given a new note, when a title is typed and saved, then it is created, tracked and closed`() = runTest {
        val viewModel = createViewModel()

        viewModel.onTitleChange("  Groceries ")
        viewModel.onBodyChange("Milk")
        viewModel.onSaveClick()

        assertEquals(NoteEditorUiState.Closed, viewModel.uiState.value)
        assertEquals(listOf(Note(id = 1, title = "Groceries", body = "Milk")), repository.observeNotes().first())
        assertEquals(listOf(AnalyticsEvent("note_created", mapOf("note_id" to "1"))), analytics.events)
    }

    @Test
    fun `given an existing note, then loads it and save stays off until something changes`() = runTest {
        repository.emit(listOf(sampleNote))

        val state = createViewModel(noteId = sampleNote.id).editing()

        assertEquals(false to sampleNote.title, state.isNewNote to state.title)
        assertEquals(false, state.canSave)
    }

    @Test
    fun `given an existing note, when edited and saved, then it is updated, tracked and closed`() = runTest {
        repository.emit(listOf(sampleNote))
        val viewModel = createViewModel(noteId = sampleNote.id)

        viewModel.onBodyChange("Milk, eggs, bread")
        viewModel.onSaveClick()

        assertEquals(NoteEditorUiState.Closed, viewModel.uiState.value)
        assertEquals("Milk, eggs, bread", repository.observeNote(sampleNote.id).first()?.body)
        assertEquals(listOf(AnalyticsEvent("note_edited", mapOf("note_id" to "4"))), analytics.events)
    }

    @Test
    fun `given the note doesn't exist, then shows not found`() = runTest {
        assertEquals(NoteEditorUiState.NotFound, createViewModel(noteId = 99).uiState.value)
    }

    @Test
    fun `given reading the note fails, then shows not found`() = runTest {
        repository.observeFailure = IllegalStateException("db closed")

        assertEquals(NoteEditorUiState.NotFound, createViewModel(noteId = 1).uiState.value)
    }

    @Test
    fun `given saving fails, then keeps the fields and shows the error until shown`() = runTest {
        repository.saveFailure = AppResult.Failure(AppError.Unknown)
        val viewModel = createViewModel()
        viewModel.onTitleChange("Groceries")

        viewModel.onSaveClick()
        assertEquals("Groceries" to AppError.Unknown, viewModel.editing().title to viewModel.editing().saveError)
        assertEquals(emptyList(), analytics.events)

        viewModel.onSaveErrorShown()
        assertNull(viewModel.editing().saveError)
    }

    @Test
    fun `given a body without title, then the title is required and save does nothing`() = runTest {
        val viewModel = createViewModel()

        viewModel.onBodyChange("Text")
        viewModel.onSaveClick()

        assertEquals(FieldError.Required, viewModel.editing().titleError)
        assertEquals(false, viewModel.editing().canSave)
        assertEquals(emptyList(), repository.observeNotes().first())
    }

    @Test
    fun `given no changes, when closed, then closes right away`() = runTest {
        val viewModel = createViewModel()

        viewModel.onCloseClick()

        assertEquals(NoteEditorUiState.Closed, viewModel.uiState.value)
    }

    @Test
    fun `given changes, when closed, then asks first, and keep editing or discard follow the choice`() = runTest {
        val viewModel = createViewModel()
        viewModel.onTitleChange("Draft")

        viewModel.onCloseClick()
        assertEquals(true, viewModel.editing().isDiscardDialogVisible)

        viewModel.onDiscardDismiss()
        assertEquals(false, viewModel.editing().isDiscardDialogVisible)

        viewModel.onCloseClick()
        viewModel.onDiscardConfirm()
        assertEquals(NoteEditorUiState.Closed, viewModel.uiState.value)
    }

    @Test
    fun `given typed text in a restored state, when recreated, then the text is kept, not reloaded`() = runTest {
        repository.emit(listOf(sampleNote))
        val handle = SavedStateHandle()
        createViewModel(noteId = sampleNote.id, savedStateHandle = handle).onTitleChange("Typed before death")

        val recreated = createViewModel(noteId = sampleNote.id, savedStateHandle = handle)

        assertEquals("Typed before death", recreated.editing().title)
        assertEquals(true, recreated.editing().canSave)
    }

    private companion object {
        val sampleNote = Note(id = 4, title = "Groceries", body = "Milk, eggs")
    }
}

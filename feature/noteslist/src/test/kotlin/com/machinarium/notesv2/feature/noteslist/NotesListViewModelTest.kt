package com.machinarium.notesv2.feature.noteslist

import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.common.result.AppResult
import com.machinarium.notesv2.core.model.Note
import com.machinarium.notesv2.core.testing.MainDispatcherRule
import com.machinarium.notesv2.core.testing.repository.FakeNotesRepository
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNotesRepository()

    private fun TestScope.createViewModel(): NotesListViewModel {
        val viewModel = NotesListViewModel(repository, NoteItemUiMapper())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `when created, then refreshes once`() = runTest {
        createViewModel()

        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun `given cached notes, when created, then shows content`() = runTest {
        repository.emit(listOf(sampleNote))

        val viewModel = createViewModel()

        val state = assertIs<NotesListUiState.Content>(viewModel.uiState.value)
        assertEquals(listOf(sampleNote.id), state.notes.map { it.id })
        assertEquals(false, state.isRefreshing)
    }

    @Test
    fun `given no notes and refresh succeeds, then shows empty`() = runTest {
        val viewModel = createViewModel()

        assertEquals(NotesListUiState.Empty(), viewModel.uiState.value)
    }

    @Test
    fun `given no notes and refresh fails, then shows error`() = runTest {
        repository.refreshResult = AppResult.Failure(AppError.Network)

        val viewModel = createViewModel()

        assertEquals(NotesListUiState.Error(AppError.Network), viewModel.uiState.value)
    }

    @Test
    fun `given cached notes and refresh fails, then keeps content with refresh error`() = runTest {
        repository.emit(listOf(sampleNote))
        repository.refreshResult = AppResult.Failure(AppError.Server(code = 503))

        val viewModel = createViewModel()

        val state = assertIs<NotesListUiState.Content>(viewModel.uiState.value)
        assertEquals(AppError.Server(code = 503), state.refreshError)
    }

    @Test
    fun `when refresh error shown, then error is cleared`() = runTest {
        repository.emit(listOf(sampleNote))
        repository.refreshResult = AppResult.Failure(AppError.Network)
        val viewModel = createViewModel()

        viewModel.onRefreshErrorShown()

        assertNull(assertIs<NotesListUiState.Content>(viewModel.uiState.value).refreshError)
    }

    @Test
    fun `given a refresh error, when refreshed again successfully, then the old error is gone`() = runTest {
        repository.emit(listOf(sampleNote))
        repository.refreshResult = AppResult.Failure(AppError.Network)
        val viewModel = createViewModel()

        repository.refreshResult = AppResult.Success(Unit)
        viewModel.onRefresh()

        assertNull(assertIs<NotesListUiState.Content>(viewModel.uiState.value).refreshError)
    }

    @Test
    fun `given an error, when retried and refresh succeeds with notes, then shows content`() = runTest {
        repository.refreshResult = AppResult.Failure(AppError.Network)
        val viewModel = createViewModel()

        repository.refreshResult = AppResult.Success(Unit)
        repository.emit(listOf(sampleNote))
        viewModel.onRefresh()

        assertIs<NotesListUiState.Content>(viewModel.uiState.value)
        assertEquals(2, repository.refreshCount)
    }

    @Test
    fun `given observing the cache fails, when retried, then recovers to content`() = runTest {
        repository.observeFailure = IllegalStateException("db closed")
        val viewModel = createViewModel()
        assertEquals(NotesListUiState.Error(AppError.Unknown), viewModel.uiState.value)

        repository.observeFailure = null
        repository.emit(listOf(sampleNote))
        viewModel.onRefresh()

        assertIs<NotesListUiState.Content>(viewModel.uiState.value)
    }

    @Test
    fun `given a note was deleted elsewhere, then content offers undo for it`() = runTest {
        repository.emit(listOf(sampleNote, otherNote))
        val viewModel = createViewModel()

        repository.deleteNote(otherNote.id)

        assertEquals(otherNote.id, viewModel.uiState.value.undoNoteId)
    }

    @Test
    fun `given the last note was deleted, then stays on empty with undo`() = runTest {
        repository.emit(listOf(sampleNote))
        val viewModel = createViewModel()

        repository.deleteNote(sampleNote.id)

        assertEquals(NotesListUiState.Empty(undoNoteId = sampleNote.id), viewModel.uiState.value)
    }

    @Test
    fun `when undo is clicked and offered, then the note is restored and undo is gone`() = runTest {
        repository.emit(listOf(sampleNote))
        val viewModel = createViewModel()
        repository.deleteNote(sampleNote.id)

        viewModel.onUndoDelete(sampleNote.id)
        viewModel.onUndoOffered()

        val state = assertIs<NotesListUiState.Content>(viewModel.uiState.value)
        assertEquals(listOf(sampleNote.id), state.notes.map { it.id })
        assertNull(state.undoNoteId)
        assertEquals(listOf(sampleNote.id), repository.restoredIds)
    }

    @Test
    fun `given restoring fails, when undo is clicked, then shows an error snackbar`() = runTest {
        repository.emit(listOf(sampleNote, otherNote))
        val viewModel = createViewModel()
        repository.deleteNote(otherNote.id)
        repository.writeFailure = AppResult.Failure(AppError.Unknown)

        viewModel.onUndoDelete(otherNote.id)

        val state = assertIs<NotesListUiState.Content>(viewModel.uiState.value)
        assertEquals(true to null, state.isRestoreFailed to state.refreshError)

        viewModel.onRestoreErrorShown()
        assertEquals(false, assertIs<NotesListUiState.Content>(viewModel.uiState.value).isRestoreFailed)
    }

    @Test
    fun `given the user has own notes, then content allows asking for notifications`() = runTest {
        repository.emit(listOf(sampleNote))
        val viewModel = createViewModel()
        assertEquals(false, assertIs<NotesListUiState.Content>(viewModel.uiState.value).canAskForNotifications)

        repository.hasUserNotes.value = true

        assertEquals(true, assertIs<NotesListUiState.Content>(viewModel.uiState.value).canAskForNotifications)
    }

    private companion object {
        val sampleNote = Note(id = 1, title = "Groceries", body = "Milk, eggs")
        val otherNote = Note(id = 2, title = "Books", body = "Dune")
    }
}

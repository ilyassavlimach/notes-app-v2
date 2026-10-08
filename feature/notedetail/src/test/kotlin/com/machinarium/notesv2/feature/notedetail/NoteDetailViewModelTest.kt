package com.machinarium.notesv2.feature.notedetail

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNotesRepository()
    private val analytics = FakeAnalyticsTracker()

    private fun TestScope.createViewModel(noteId: Long = sampleNote.id): NoteDetailViewModel {
        val viewModel = NoteDetailViewModel(noteId, repository, analytics)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `given a stored note, when created, then shows it and tracks note_opened once`() = runTest {
        repository.emit(listOf(sampleNote))

        val viewModel = createViewModel()

        assertEquals(NoteDetailUiState.Content(sampleNote.title, sampleNote.body), viewModel.uiState.value)
        assertEquals(listOf(event("note_opened")), analytics.events)
    }

    @Test
    fun `given no note with the id, then shows not found`() = runTest {
        repository.emit(listOf(sampleNote))

        val viewModel = createViewModel(noteId = 99)

        assertEquals(NoteDetailUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun `given the note changes, then shows the new content`() = runTest {
        repository.emit(listOf(sampleNote))
        val viewModel = createViewModel()

        repository.emit(listOf(sampleNote.copy(body = "Updated")))

        assertEquals(NoteDetailUiState.Content(sampleNote.title, "Updated"), viewModel.uiState.value)
    }

    @Test
    fun `given reading the note fails, then shows a typed error, and retry recovers`() = runTest {
        repository.observeFailure = IllegalStateException("db closed")
        repository.emit(listOf(sampleNote))
        val viewModel = createViewModel()
        assertEquals(NoteDetailUiState.Error(AppError.Unknown), viewModel.uiState.value)

        repository.observeFailure = null
        viewModel.onRetry()

        assertIs<NoteDetailUiState.Content>(viewModel.uiState.value)
    }

    @Test
    fun `when delete is clicked and dismissed, then the dialog opens and closes`() = runTest {
        repository.emit(listOf(sampleNote))
        val viewModel = createViewModel()

        viewModel.onDeleteClick()
        assertEquals(true, content(viewModel).isDeleteDialogVisible)

        viewModel.onDeleteDismiss()
        assertEquals(false, content(viewModel).isDeleteDialogVisible)
    }

    @Test
    fun `when delete is confirmed, then the note is deleted, tracked and the screen closes`() = runTest {
        repository.emit(listOf(sampleNote))
        val viewModel = createViewModel()
        viewModel.onDeleteClick()

        viewModel.onDeleteConfirm()

        assertEquals(NoteDetailUiState.Deleted, viewModel.uiState.value)
        assertEquals(sampleNote.id, repository.recentlyDeleted.value)
        assertEquals(listOf(event("note_opened"), event("note_deleted")), analytics.events)
    }

    @Test
    fun `given delete fails, when confirmed, then shows the error until it was shown`() = runTest {
        repository.emit(listOf(sampleNote))
        repository.writeFailure = AppResult.Failure(AppError.Unknown)
        val viewModel = createViewModel()

        viewModel.onDeleteConfirm()
        assertEquals(AppError.Unknown, content(viewModel).deleteError)
        assertEquals(listOf(event("note_opened")), analytics.events)

        viewModel.onDeleteErrorShown()
        assertNull(content(viewModel).deleteError)
    }

    private fun content(viewModel: NoteDetailViewModel) = assertIs<NoteDetailUiState.Content>(viewModel.uiState.value)

    private fun event(name: String) = AnalyticsEvent(name, mapOf("note_id" to sampleNote.id.toString()))

    private companion object {
        val sampleNote = Note(id = 1, title = "Groceries", body = "Milk, eggs")
    }
}

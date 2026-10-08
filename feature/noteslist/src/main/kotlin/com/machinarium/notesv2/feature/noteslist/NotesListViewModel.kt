package com.machinarium.notesv2.feature.noteslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.common.result.AppResult
import com.machinarium.notesv2.core.data.repository.NotesRepository
import com.machinarium.notesv2.core.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class NotesListViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val noteItemMapper: NoteItemUiMapper,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val refreshError = MutableStateFlow<AppError?>(null)
    private val observeAttempt = MutableStateFlow(0)

    // Each refresh re-subscribes to the cache, so a failed observation can recover through Retry.
    private val notes: Flow<List<Note>?> = observeAttempt.flatMapLatest {
        notesRepository.observeNotes()
            .map<List<Note>, List<Note>?> { it }
            .catch { emit(null) }
    }

    val uiState: StateFlow<NotesListUiState> = combine(
        notes,
        isRefreshing,
        refreshError,
        notesRepository.recentlyDeleted,
    ) { notes, refreshing, error, undoNoteId ->
        if (notes == null) NotesListUiState.Error(AppError.Unknown) else toUiState(notes, refreshing, error, undoNoteId)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = NotesListUiState.Loading,
        )

    init {
        onRefresh()
    }

    fun onRefresh() {
        if (isRefreshing.value) return
        observeAttempt.value++
        viewModelScope.launch {
            isRefreshing.value = true
            refreshError.value = null
            refreshError.value = (notesRepository.refresh() as? AppResult.Failure)?.error
            isRefreshing.value = false
        }
    }

    fun onRefreshErrorShown() {
        refreshError.value = null
    }

    fun onUndoDelete(noteId: Long) {
        viewModelScope.launch {
            if (notesRepository.restoreNote(noteId) is AppResult.Failure) refreshError.value = AppError.Unknown
        }
    }

    /** The Undo snackbar was shown (and acted on or dismissed), so it isn't offered again. */
    fun onUndoOffered() {
        notesRepository.consumeRecentlyDeleted()
    }

    private fun toUiState(
        notes: List<Note>,
        refreshing: Boolean,
        error: AppError?,
        undoNoteId: Long?,
    ): NotesListUiState = when {
        notes.isNotEmpty() -> NotesListUiState.Content(
            notes = notes.map(noteItemMapper::map).toImmutableList(),
            isRefreshing = refreshing,
            refreshError = error,
            undoNoteId = undoNoteId,
        )
        // The last note was just deleted: stay on the (refreshable) empty list so Undo is still offered.
        undoNoteId != null -> NotesListUiState.Empty(undoNoteId)
        refreshing -> NotesListUiState.Loading
        error != null -> NotesListUiState.Error(error)
        else -> NotesListUiState.Empty()
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

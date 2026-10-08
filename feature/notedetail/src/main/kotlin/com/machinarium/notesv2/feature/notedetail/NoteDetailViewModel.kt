package com.machinarium.notesv2.feature.notedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machinarium.notesv2.core.analytics.AnalyticsEvent
import com.machinarium.notesv2.core.analytics.AnalyticsTracker
import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.common.result.AppResult
import com.machinarium.notesv2.core.data.repository.NotesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
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
@HiltViewModel(assistedFactory = NoteDetailViewModel.Factory::class)
internal class NoteDetailViewModel @AssistedInject constructor(
    @Assisted private val noteId: Long,
    private val notesRepository: NotesRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val observeAttempt = MutableStateFlow(0)
    private val isDeleteDialogVisible = MutableStateFlow(false)
    private val isDeleted = MutableStateFlow(false)
    private val deleteError = MutableStateFlow<AppError?>(null)

    private val noteState: Flow<NoteDetailUiState> = observeAttempt.flatMapLatest {
        notesRepository.observeNote(noteId)
            .map { note -> note?.let { NoteDetailUiState.Content(it.title, it.body) } ?: NoteDetailUiState.NotFound }
            .catch { emit(NoteDetailUiState.Error(AppError.Unknown)) }
    }

    val uiState: StateFlow<NoteDetailUiState> =
        combine(noteState, isDeleteDialogVisible, isDeleted, deleteError) { state, dialogVisible, deleted, error ->
            when {
                // Checked first: once deleted, the note disappears and must not flash "not found" before closing.
                deleted -> NoteDetailUiState.Deleted
                state is NoteDetailUiState.Content -> state.copy(
                    isDeleteDialogVisible = dialogVisible,
                    deleteError = error,
                )
                else -> state
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = NoteDetailUiState.Loading,
        )

    init {
        track(EVENT_NOTE_OPENED)
    }

    fun onRetry() {
        observeAttempt.value++
    }

    fun onDeleteClick() {
        isDeleteDialogVisible.value = true
    }

    fun onDeleteDismiss() {
        isDeleteDialogVisible.value = false
    }

    fun onDeleteConfirm() {
        isDeleteDialogVisible.value = false
        viewModelScope.launch {
            when (val result = notesRepository.deleteNote(noteId)) {
                is AppResult.Success -> {
                    track(EVENT_NOTE_DELETED)
                    isDeleted.value = true
                }
                is AppResult.Failure -> deleteError.value = result.error
            }
        }
    }

    fun onDeleteErrorShown() {
        deleteError.value = null
    }

    private fun track(eventName: String) {
        analyticsTracker.track(AnalyticsEvent(eventName, mapOf(PARAM_NOTE_ID to noteId.toString())))
    }

    @AssistedFactory
    interface Factory {
        fun create(noteId: Long): NoteDetailViewModel
    }

    companion object {
        internal const val EVENT_NOTE_OPENED = "note_opened"
        internal const val EVENT_NOTE_DELETED = "note_deleted"
        internal const val PARAM_NOTE_ID = "note_id"
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

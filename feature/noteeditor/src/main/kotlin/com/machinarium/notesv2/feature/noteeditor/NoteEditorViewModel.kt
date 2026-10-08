package com.machinarium.notesv2.feature.noteeditor

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Creates a note ([noteId] = null) or edits one. Typed text lives in [SavedStateHandle], so it survives process death. */
@HiltViewModel(assistedFactory = NoteEditorViewModel.Factory::class)
internal class NoteEditorViewModel @AssistedInject constructor(
    @Assisted private val noteId: Long?,
    private val savedStateHandle: SavedStateHandle,
    private val notesRepository: NotesRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val title = savedStateHandle.getStateFlow<String?>(KEY_TITLE, null)
    private val body = savedStateHandle.getStateFlow(KEY_BODY, "")
    private val originalTitle = savedStateHandle.getStateFlow(KEY_ORIGINAL_TITLE, "")
    private val originalBody = savedStateHandle.getStateFlow(KEY_ORIGINAL_BODY, "")
    private val status = MutableStateFlow(EditorStatus())

    val uiState: StateFlow<NoteEditorUiState> =
        combine(title, body, originalTitle, originalBody, status) { title, body, originalTitle, originalBody, status ->
            when {
                status.isClosed -> NoteEditorUiState.Closed
                status.isNotFound -> NoteEditorUiState.NotFound
                title == null -> NoteEditorUiState.Loading
                else -> NoteEditorUiState.Editing(
                    isNewNote = noteId == null,
                    title = title,
                    body = body,
                    titleError = NoteFieldValidator.titleError(title, body),
                    bodyError = NoteFieldValidator.bodyError(body),
                    canSave = (title != originalTitle || body != originalBody) &&
                        !status.isSaving &&
                        NoteFieldValidator.isSavable(title, body),
                    isSaving = status.isSaving,
                    isDiscardDialogVisible = status.isDiscardDialogVisible,
                    saveError = status.saveError,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = NoteEditorUiState.Loading,
        )

    init {
        // A restored SavedStateHandle already holds the fields; loading again would drop the user's typing.
        if (title.value == null) loadFields()
    }

    fun onTitleChange(value: String) {
        savedStateHandle[KEY_TITLE] = value
    }

    fun onBodyChange(value: String) {
        savedStateHandle[KEY_BODY] = value
    }

    fun onSaveClick() {
        val state = uiState.value as? NoteEditorUiState.Editing ?: return
        // status is checked directly: uiState is derived and may not show isSaving yet on a fast second tap.
        if (!state.canSave || status.value.isSaving || status.value.isClosed) return
        status.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            val savedId = save(state.title.trim(), state.body.trim())
            status.update {
                when (savedId) {
                    is AppResult.Success -> it.copy(isSaving = false, isClosed = true)
                    is AppResult.Failure -> it.copy(isSaving = false, saveError = savedId.error)
                }
            }
        }
    }

    fun onCloseClick() {
        status.update { if (hasChanges()) it.copy(isDiscardDialogVisible = true) else it.copy(isClosed = true) }
    }

    fun onDiscardConfirm() {
        status.update { it.copy(isDiscardDialogVisible = false, isClosed = true) }
    }

    fun onDiscardDismiss() {
        status.update { it.copy(isDiscardDialogVisible = false) }
    }

    fun onSaveErrorShown() {
        status.update { it.copy(saveError = null) }
    }

    /** Creates or updates the note and tracks it; returns the saved note's id. */
    private suspend fun save(
        title: String,
        body: String,
    ): AppResult<Long> {
        val result = if (noteId == null) {
            notesRepository.createNote(title, body)
        } else {
            when (val update = notesRepository.updateNote(noteId, title, body)) {
                is AppResult.Success -> AppResult.Success(noteId)
                is AppResult.Failure -> update
            }
        }
        if (result is AppResult.Success) {
            val eventName = if (noteId == null) EVENT_NOTE_CREATED else EVENT_NOTE_EDITED
            analyticsTracker.track(AnalyticsEvent(eventName, mapOf(PARAM_NOTE_ID to result.data.toString())))
        }
        return result
    }

    private fun hasChanges(): Boolean {
        val currentTitle = title.value ?: return false
        return currentTitle != originalTitle.value || body.value != originalBody.value
    }

    private fun loadFields() {
        if (noteId == null) {
            setFields(title = "", body = "")
            return
        }
        viewModelScope.launch {
            val note = notesRepository.observeNote(noteId)
                .catch { emit(null) } // a failed read is treated like a missing note: nothing to edit
                .first()
            if (note == null) status.update { it.copy(isNotFound = true) } else setFields(note.title, note.body)
        }
    }

    private fun setFields(
        title: String,
        body: String,
    ) {
        savedStateHandle[KEY_ORIGINAL_TITLE] = title
        savedStateHandle[KEY_ORIGINAL_BODY] = body
        savedStateHandle[KEY_BODY] = body
        savedStateHandle[KEY_TITLE] = title // last: a non-null title means "loaded"
    }

    private data class EditorStatus(
        val isSaving: Boolean = false,
        val isDiscardDialogVisible: Boolean = false,
        val isClosed: Boolean = false,
        val isNotFound: Boolean = false,
        val saveError: AppError? = null,
    )

    @AssistedFactory
    interface Factory {
        fun create(noteId: Long?): NoteEditorViewModel
    }

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_ORIGINAL_TITLE = "original_title"
        const val KEY_ORIGINAL_BODY = "original_body"
        const val EVENT_NOTE_CREATED = "note_created"
        const val EVENT_NOTE_EDITED = "note_edited"
        const val PARAM_NOTE_ID = "note_id"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

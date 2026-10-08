package com.machinarium.notesv2.feature.noteslist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import com.machinarium.notesv2.core.i18n.R
import com.machinarium.notesv2.core.testing.resource.stringResource
import kotlin.test.assertEquals
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotesListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun contentState_showsNoteTitles() {
        setScreen(content())

        composeRule.onNodeWithText(PreviewNotes.first().title).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.noteslist_title)).assertIsDisplayed()
    }

    @Test
    fun contentState_noteClick_passesNoteId() {
        var clickedId: Long? = null
        setScreen(content(), onNoteClick = { clickedId = it })

        composeRule.onNodeWithText(PreviewNotes.last().title).performClick()

        assertEquals(PreviewNotes.last().id, clickedId)
    }

    @Test
    fun contentStateWithRefreshError_showsSnackbar_andReportsShown() {
        var shownCalls = 0
        setScreen(content(refreshError = AppError.Network), onRefreshErrorShown = { shownCalls++ })

        composeRule.onNodeWithText(stringResource(R.string.noteslist_refresh_failed)).assertIsDisplayed()
        composeRule.mainClock.advanceTimeBy(SNACKBAR_TIMEOUT_MILLIS)
        composeRule.waitForIdle()
        assertEquals(1, shownCalls)
    }

    @Test
    fun errorState_showsMessage_andRetryInvokesRefresh() {
        var refreshCalls = 0
        setScreen(NotesListUiState.Error(AppError.Network), onRefresh = { refreshCalls++ })

        composeRule.onNodeWithText(stringResource(R.string.common_error_network)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.common_retry)).performClick()

        assertEquals(1, refreshCalls)
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        setScreen(NotesListUiState.Empty())

        composeRule.onNodeWithText(stringResource(R.string.noteslist_empty)).assertIsDisplayed()
    }

    @Test
    fun emptyState_pullDown_invokesRefresh() {
        var refreshCalls = 0
        setScreen(NotesListUiState.Empty(), onRefresh = { refreshCalls++ })

        composeRule.onNodeWithText(stringResource(R.string.noteslist_empty)).assertIsDisplayed()
        composeRule.onNode(hasScrollToIndexAction()).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        assertEquals(1, refreshCalls)
    }

    @Test
    fun deletedNote_undoClick_restoresIt_andReportsOffered() {
        var undoneId: Long? = null
        var offeredCalls = 0
        setScreen(
            NotesListUiState.Empty(undoNoteId = DELETED_ID),
            onUndoDelete = { undoneId = it },
            onUndoOffered = { offeredCalls++ },
        )

        composeRule.onNodeWithText(stringResource(R.string.noteslist_note_deleted)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.noteslist_undo)).performClick()
        composeRule.waitForIdle()

        assertEquals(DELETED_ID to 1, undoneId to offeredCalls)
    }

    @Test
    fun loadingState_showsProgress() {
        setScreen(NotesListUiState.Loading)

        composeRule.onNode(hasContentDescription(stringResource(R.string.common_loading)))
            .assertIsDisplayed()
    }

    private fun content(
        refreshError: AppError? = null,
        notes: ImmutableList<NoteItemUi> = PreviewNotes,
    ) = NotesListUiState.Content(notes, isRefreshing = false, refreshError = refreshError)

    @Test
    fun scrolledList_whenANoteIsAddedAtTheTop_scrollsUpToShowIt() { // regression: new note was off-screen
        var state by mutableStateOf(content(notes = manyNotes))
        composeRule.setContent {
            NotesV2Theme {
                NotesListScreen(
                    uiState = state,
                    isNotificationPermissionGranted = true,
                    onNoteClick = {},
                    onAddNoteClick = {},
                    onRefresh = {},
                    onRefreshErrorShown = {},
                    onRestoreErrorShown = {},
                    onUndoDelete = {},
                    onUndoOffered = {},
                    onAllowNotificationsClick = {},
                )
            }
        }
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(manyNotes.lastIndex)

        state = content(notes = (listOf(newNote) + manyNotes).toImmutableList())
        composeRule.waitForIdle()

        composeRule.onNodeWithText(newNote.title).assertIsDisplayed()
    }

    @Test
    fun restoreFailed_showsUndoFailedMessage_andReportsShown() { // regression: said "couldn't refresh"
        var shown = 0
        setScreen(content().copy(isRestoreFailed = true), onRestoreErrorShown = { shown++ })

        composeRule.onNodeWithText(stringResource(R.string.noteslist_undo_failed)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.noteslist_refresh_failed)).assertDoesNotExist()
        composeRule.mainClock.advanceTimeBy(SNACKBAR_TIMEOUT_MILLIS)
        composeRule.waitForIdle()

        assertEquals(1, shown)
    }

    @Test
    fun addNoteClick_invokesCallback() {
        var addCalls = 0
        setScreen(content(), onAddNoteClick = { addCalls++ })

        composeRule.onNodeWithContentDescription(stringResource(R.string.noteslist_add_note)).performClick()

        assertEquals(1, addCalls)
    }

    @Test
    fun rationale_allowClick_requestsPermission_andHidesCard() {
        var requests = 0
        setScreen(
            content().copy(canAskForNotifications = true),
            isPermissionGranted = false,
            onAllowNotificationsClick = { requests++ },
        )

        composeRule.onNodeWithText(stringResource(R.string.noteslist_notifications_rationale)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResource(R.string.noteslist_notifications_allow)).performClick()

        assertEquals(1, requests)
        composeRule.onNodeWithText(stringResource(R.string.noteslist_notifications_rationale)).assertDoesNotExist()
    }

    @Test
    fun rationale_notNow_hidesCardWithoutRequesting() {
        var requests = 0
        setScreen(
            content().copy(canAskForNotifications = true),
            isPermissionGranted = false,
            onAllowNotificationsClick = { requests++ },
        )

        composeRule.onNodeWithText(stringResource(R.string.noteslist_notifications_not_now)).performClick()

        assertEquals(0, requests)
        composeRule.onNodeWithText(stringResource(R.string.noteslist_notifications_rationale)).assertDoesNotExist()
    }

    @Test
    fun rationale_isHidden_whenGrantedOrNoUserNotes() {
        setScreen(content().copy(canAskForNotifications = true), isPermissionGranted = true)

        composeRule.onNodeWithText(stringResource(R.string.noteslist_notifications_rationale)).assertDoesNotExist()
    }

    private fun setScreen(
        state: NotesListUiState,
        isPermissionGranted: Boolean = true,
        onAddNoteClick: () -> Unit = {},
        onAllowNotificationsClick: () -> Unit = {},
        onNoteClick: (Long) -> Unit = {},
        onRefresh: () -> Unit = {},
        onRefreshErrorShown: () -> Unit = {},
        onRestoreErrorShown: () -> Unit = {},
        onUndoDelete: (Long) -> Unit = {},
        onUndoOffered: () -> Unit = {},
    ) {
        composeRule.setContent {
            NotesV2Theme {
                NotesListScreen(
                    uiState = state,
                    isNotificationPermissionGranted = isPermissionGranted,
                    onNoteClick = onNoteClick,
                    onAddNoteClick = onAddNoteClick,
                    onRefresh = onRefresh,
                    onRefreshErrorShown = onRefreshErrorShown,
                    onRestoreErrorShown = onRestoreErrorShown,
                    onUndoDelete = onUndoDelete,
                    onUndoOffered = onUndoOffered,
                    onAllowNotificationsClick = onAllowNotificationsClick,
                )
            }
        }
    }

    private companion object {
        const val SNACKBAR_TIMEOUT_MILLIS = 10_000L
        const val DELETED_ID = 5L
        val manyNotes = (1L..30L).map {
            NoteItemUi(id = it, title = "Note $it", preview = "Body $it")
        }.toImmutableList()
        val newNote = NoteItemUi(id = 99, title = "Brand new note", preview = "Just saved")
    }
}

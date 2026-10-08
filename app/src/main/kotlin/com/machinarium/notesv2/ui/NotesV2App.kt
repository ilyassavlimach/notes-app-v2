package com.machinarium.notesv2.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.machinarium.notesv2.core.config.AppGateState
import com.machinarium.notesv2.core.config.ui.AppGateScreen
import com.machinarium.notesv2.feature.notedetail.navigation.NoteDetailKey
import com.machinarium.notesv2.feature.notedetail.navigation.noteDetailEntry
import com.machinarium.notesv2.feature.noteeditor.navigation.NoteEditorKey
import com.machinarium.notesv2.feature.noteeditor.navigation.noteEditorEntry
import com.machinarium.notesv2.feature.noteslist.navigation.NotesListKey
import com.machinarium.notesv2.feature.noteslist.navigation.notesListEntry
import com.machinarium.notesv2.ui.navigation.goBack
import com.machinarium.notesv2.ui.navigation.navigateTo

/**
 * Root composable: owns the Navigation 3 back stack and wires feature entries together. Features never touch the
 * back stack — they expose callbacks and this file connects them (ARCH-01, ARCH-08).
 *
 * A deep link (from the launch intent or one that arrives while the app runs) comes in as [pendingDeepLink] and
 * replaces the back stack; [onDeepLinkHandled] clears it so it isn't applied twice.
 */
@Composable
internal fun NotesV2App(
    pendingDeepLink: List<NavKey>?,
    onDeepLinkHandled: () -> Unit,
    modifier: Modifier = Modifier,
    appGateViewModel: AppGateViewModel = hiltViewModel(),
) {
    val gateState by appGateViewModel.state.collectAsStateWithLifecycle()
    if (gateState == AppGateState.Open) {
        NotesV2NavDisplay(pendingDeepLink, onDeepLinkHandled, modifier)
    } else {
        AppGate(gateState, modifier)
    }
}

@Composable
private fun AppGate(
    state: AppGateState,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val packageName = LocalContext.current.packageName
    AppGateScreen(
        state = state,
        onUpdateClick = {
            // Play Store app first; the web page when there is no store app (e.g. some emulators).
            runCatching { uriHandler.openUri("$MARKET_URI$packageName") }
                .onFailure { uriHandler.openUri("$PLAY_WEB_URI$packageName") }
        },
        modifier = modifier,
    )
}

@Composable
private fun NotesV2NavDisplay(
    pendingDeepLink: List<NavKey>?,
    onDeepLinkHandled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(NotesListKey)
    LaunchedEffect(pendingDeepLink) {
        pendingDeepLink ?: return@LaunchedEffect
        backStack.clear()
        backStack.addAll(pendingDeepLink)
        onDeepLinkHandled()
    }
    NavDisplay(
        backStack = backStack,
        modifier = modifier.fillMaxSize(),
        onBack = { backStack.goBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // Scopes each entry's ViewModels to the entry, so they are cleared when it leaves the back stack.
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            notesListEntry(
                onNoteClick = { id -> backStack.navigateTo(NoteDetailKey(id)) },
                onAddNoteClick = { backStack.navigateTo(NoteEditorKey()) },
            )
            noteDetailEntry(
                onBack = { backStack.goBack() },
                onEditClick = { id -> backStack.navigateTo(NoteEditorKey(id)) },
            )
            noteEditorEntry(onClose = { backStack.goBack() })
        },
    )
}

private const val MARKET_URI = "market://details?id="
private const val PLAY_WEB_URI = "https://play.google.com/store/apps/details?id="

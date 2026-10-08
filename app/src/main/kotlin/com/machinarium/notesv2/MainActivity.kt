package com.machinarium.notesv2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.NavKey
import com.machinarium.notesv2.core.designsystem.theme.NotesV2Theme
import com.machinarium.notesv2.ui.NotesV2App
import com.machinarium.notesv2.ui.navigation.DeepLinkParser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val deepLinkParser = DeepLinkParser(host = BuildConfig.DEEP_LINK_HOST)
    private var pendingDeepLink by mutableStateOf<List<NavKey>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // On a recreation the restored back stack wins; the launch link is applied only once.
        if (savedInstanceState == null) intent?.data?.let { pendingDeepLink = deepLinkParser.parse(it) }
        setContent {
            NotesV2Theme {
                NotesV2App(
                    pendingDeepLink = pendingDeepLink,
                    onDeepLinkHandled = { pendingDeepLink = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // LINK-01: a link opened while the app is running replaces the back stack.
        intent.data?.let { pendingDeepLink = deepLinkParser.parse(it) }
    }
}

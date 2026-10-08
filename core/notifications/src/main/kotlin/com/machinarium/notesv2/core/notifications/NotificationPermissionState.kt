package com.machinarium.notesv2.core.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** State of POST_NOTIFICATIONS (PUSH-01). Below API 33 notifications need no runtime permission. */
@Stable
class NotificationPermissionState internal constructor(
    private val granted: MutableState<Boolean>,
    private val launch: () -> Unit,
) {
    val isGranted: Boolean get() = granted.value

    /** Call after the feature has shown its own explanation (rationale) — never on app start. */
    fun request() {
        if (!isGranted) launch()
    }
}

@Composable
fun rememberNotificationPermissionState(onResult: (granted: Boolean) -> Unit = {}): NotificationPermissionState {
    val context = LocalContext.current
    val granted = remember { mutableStateOf(context.hasNotificationPermission()) }
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        granted.value = result
        currentOnResult(result)
    }
    return remember(launcher) {
        NotificationPermissionState(granted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

private fun Context.hasNotificationPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

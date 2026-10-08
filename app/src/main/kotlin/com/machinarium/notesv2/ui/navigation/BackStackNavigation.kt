package com.machinarium.notesv2.ui.navigation

import androidx.navigation3.runtime.NavKey

/** ARCH-10: adding the key that is already on top is a no-op, so a double tap never stacks duplicates. */
fun MutableList<NavKey>.navigateTo(key: NavKey) {
    if (lastOrNull() != key) add(key)
}

/** ARCH-10: back never removes the root, so the NavDisplay can't end up empty. */
fun MutableList<NavKey>.goBack() {
    if (size > 1) removeAt(lastIndex)
}

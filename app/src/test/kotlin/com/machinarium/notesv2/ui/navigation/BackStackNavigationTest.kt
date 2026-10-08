package com.machinarium.notesv2.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.test.assertEquals
import org.junit.Test

class BackStackNavigationTest {

    private data object Home : NavKey

    private data class Detail(val id: Long) : NavKey

    @Test
    fun `when navigating to a new key, then it is pushed`() {
        val backStack = mutableListOf<NavKey>(Home)

        backStack.navigateTo(Detail(1))

        assertEquals(listOf(Home, Detail(1)), backStack)
    }

    @Test
    fun `when navigating to the key already on top, then nothing is pushed`() {
        val backStack = mutableListOf(Home, Detail(1))

        backStack.navigateTo(Detail(1))

        assertEquals(listOf(Home, Detail(1)), backStack)
    }

    @Test
    fun `when going back, then the top key is removed`() {
        val backStack = mutableListOf(Home, Detail(1))

        backStack.goBack()

        assertEquals(listOf<NavKey>(Home), backStack)
    }

    @Test
    fun `when going back on the root, then the root stays`() {
        val backStack = mutableListOf<NavKey>(Home)

        backStack.goBack()

        assertEquals(listOf<NavKey>(Home), backStack)
    }
}

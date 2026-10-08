package com.machinarium.notesv2.core.config

import kotlin.test.assertEquals
import org.junit.Test

class AppGateTest {

    @Test
    fun `given an up-to-date version and no maintenance, then the app is open`() {
        assertEquals(AppGateState.Open, AppGate(minVersionCode = 5, isMaintenance = false).evaluate(5))
    }

    @Test
    fun `given an older version, then an update is required`() {
        assertEquals(AppGateState.UpdateRequired, AppGate(minVersionCode = 6, isMaintenance = false).evaluate(5))
    }

    @Test
    fun `given maintenance and an older version, then maintenance wins`() {
        assertEquals(AppGateState.Maintenance, AppGate(minVersionCode = 6, isMaintenance = true).evaluate(5))
    }
}

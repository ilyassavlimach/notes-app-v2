package com.machinarium.notesv2.core.ui

import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.i18n.R
import kotlin.test.assertEquals
import org.junit.Test

class AppErrorMessageTest {

    @Test
    fun `network error maps to the network message`() {
        assertEquals(R.string.common_error_network, AppError.Network.messageRes())
    }

    @Test
    fun `any server status maps to the server message`() {
        assertEquals(R.string.common_error_server, AppError.Server(code = 503).messageRes())
    }

    @Test
    fun `unknown error maps to the generic message`() {
        assertEquals(R.string.common_error_unknown, AppError.Unknown.messageRes())
    }
}

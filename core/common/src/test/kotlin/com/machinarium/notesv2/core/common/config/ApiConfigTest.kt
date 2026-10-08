package com.machinarium.notesv2.core.common.config

import kotlin.test.assertEquals
import org.junit.Test

class ApiConfigTest {

    @Test
    fun `given comma-separated pins with spaces, then each pin is trimmed`() {
        val config = ApiConfig.from("https://api.example.com/", " sha256/AAA= , sha256/BBB=")

        assertEquals(listOf("sha256/AAA=", "sha256/BBB="), config.certPins)
    }

    @Test
    fun `given an empty pins property, then pinning is off`() {
        assertEquals(emptyList(), ApiConfig.from("https://api.example.com/", "").certPins)
    }

    @Test
    fun `given stray commas, then blank entries are ignored`() {
        assertEquals(listOf("sha256/AAA="), ApiConfig.from("https://api.example.com/", ",sha256/AAA=,,").certPins)
    }
}

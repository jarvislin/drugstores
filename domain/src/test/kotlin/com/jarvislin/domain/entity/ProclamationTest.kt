package com.jarvislin.domain.entity

import org.junit.Test

import org.junit.Assert.*
import java.util.*

class ProclamationTest {
    private val proclamation = Proclamation(
        createdAt = Date(),
        expiredAt = Date(),
        image = "",
        text = ""
    )

    @Test
    fun isValid() {
        assertFalse(proclamation.copy(expiredAt = Date(0)).isValid())
        assertTrue(proclamation.copy(expiredAt = Date(4102444800000L)).isValid())
    }
}
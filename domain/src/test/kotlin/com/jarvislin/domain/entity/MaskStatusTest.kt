package com.jarvislin.domain.entity

import org.junit.Assert.*
import org.junit.Test

class MaskStatusTest {

    @Test
    fun from() {
        assertEquals(Status.Empty, Status.from(Status.Empty.value))
        assertEquals(Status.Sufficient, Status.from(Status.Sufficient.value))
        assertEquals(Status.Warning, Status.from(Status.Warning.value))
        assertNull(Status.from(123))
    }
}
package com.jarvislin.domain.entity

import org.junit.Test

import org.junit.Assert.*

class DrugstoreInfoTest {
    private val info = DrugstoreInfo(
        id = "5945010154",
        name ="藥局",
        lat = 23.990268,
        lng = 121.624669,
        updateAt = "2020/11/15 21:41:27",
        adultMaskAmount = 123,
        childMaskAmount = 456,
        note = "",
        available = "NNNNNNNNNNNNNNNNNNNNN",
        address = "花蓮縣花蓮市中美路９５號之３",
        phone = "(03)1234567890"
    )

    @Test
    fun isValidOpenTime() {
        assertTrue(info.isValidOpenTime())
    }

    @Test
    fun isInvalidOpenTime() {
        assertFalse(info.copy(available = "YYYYYNNNN").isValidOpenTime())
    }
}
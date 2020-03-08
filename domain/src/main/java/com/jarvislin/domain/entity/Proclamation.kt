package com.jarvislin.domain.entity

import java.io.Serializable
import java.util.*

data class Proclamation(
    val createdAt: Date,
    val expiredAt: Date,
    val image: String?,
    val text: String?
) : Serializable {
    fun isValid(): Boolean {
        return System.currentTimeMillis() <= expiredAt.time
    }
}
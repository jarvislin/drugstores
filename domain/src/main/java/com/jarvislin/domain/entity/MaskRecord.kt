package com.jarvislin.domain.entity

import java.io.Serializable
import java.util.*

data class MaskRecord(
    val adultAmount: Int,
    val childAmount: Int,
    val date: Date
) : Serializable
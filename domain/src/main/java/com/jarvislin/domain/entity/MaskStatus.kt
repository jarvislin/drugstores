package com.jarvislin.domain.entity

import java.util.*

data class MaskStatus(val status: Status, val date: Date)

enum class Status(val value: Long) {
    Empty(0), Warning(1), Sufficient(2);

    companion object {
        private val map = values().associateBy(Status::value)
        fun from(type: Long?) = map[type]
    }
}
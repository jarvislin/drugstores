package com.jarvislin.domain.entity

import java.util.*

data class MaskStatus(val status: Status, val date: Date) {
    fun getReportWording(): String {
        val current = System.currentTimeMillis()
        val diffHour = (current - date.time) / 60 / 60 / 1000
        val diffMinute = (current - date.time) / 60 / 1000
        val diffSecond = (current - date.time) / 1000
        return when {
            diffHour > 0 -> "回報於 $diffHour 小時前"
            diffMinute > 0 -> "回報於 $diffMinute 分鐘前"
            else -> "回報於 $diffSecond 秒前"
        }
    }
}

enum class Status(val value: Long) {
    Empty(0), Warning(1), Sufficient(2);

    companion object {
        private val map = values().associateBy(Status::value)
        fun from(type: Long?) = map[type]
    }
}
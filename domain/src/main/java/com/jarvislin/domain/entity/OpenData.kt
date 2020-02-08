package com.jarvislin.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "OpenData")
data class OpenData(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "drugstore_id")
    val id: String,
    @ColumnInfo(name = "adult_mask_amount")
    val adultMaskAmount: Int,
    @ColumnInfo(name = "child_mask_amount")
    val childMaskAmount: Int,
    @ColumnInfo(name = "update_at")
    val updateAt: String
) : Serializable {
    fun getUpdateText(): String {
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        return try {
            val current = System.currentTimeMillis()
            val date = format.parse(updateAt)
            val diffHour = (current - date.time) / 60 / 60 / 1000
            val diffMinute = (current - date.time) / 60 / 1000
            val diffSecond = (current - date.time) / 1000
            when {
                diffHour > 0 -> "資料更新於 $diffHour 小時前"
                diffMinute > 0 -> "資料更新於 $diffMinute 分鐘前"
                else -> "資料更新於 $diffSecond 秒前"
            }
        } catch (ex: Exception) {
            "更新於：$updateAt"
        }
    }
}
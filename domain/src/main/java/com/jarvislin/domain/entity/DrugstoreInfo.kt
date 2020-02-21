package com.jarvislin.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "DrugstoreInfo")
data class DrugstoreInfo(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "lat")
    val lat: Double,
    @ColumnInfo(name = "lng")
    val lng: Double,
    @ColumnInfo(name = "update_at")
    val updateAt: String,
    @ColumnInfo(name = "adult_mask_amount")
    val adultMaskAmount: Int,
    @ColumnInfo(name = "child_mask_amount")
    val childMaskAmount: Int,
    @ColumnInfo(name = "note")
    val note: String,
    @ColumnInfo(name = "available")
    val available: String,
    @ColumnInfo(name = "address")
    val address: String,
    @ColumnInfo(name = "phone")
    val phone: String
) : Serializable {

    fun getUpdateWording(): String {
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
            if (updateAt.isEmpty()) {
                "無更新時間"
            } else {
                "更新於：$this"
            }
        }
    }
}
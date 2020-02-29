package com.jarvislin.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

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
) : Serializable, Item {
    fun isValidOpenTime() = Regex("[YN]{21}").matches(available)
}

interface Item
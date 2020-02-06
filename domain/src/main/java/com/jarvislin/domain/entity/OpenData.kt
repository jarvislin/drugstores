package com.jarvislin.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
)
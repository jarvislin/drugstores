package com.jarvislin.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "Drugstores")
data class Drugstore(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    @SerializedName("id")
    val id: String,
    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String,
    @ColumnInfo(name = "lat")
    @SerializedName("lat")
    val lat: Double,
    @ColumnInfo(name = "lng")
    @SerializedName("lng")
    val lng: Double,
    @ColumnInfo(name = "address")
    @SerializedName("address")
    val address: String,
    @ColumnInfo(name = "phone")
    @SerializedName("phone")
    val phone: String,
    @ColumnInfo(name = "note")
    @SerializedName("note")
    val note: String
) : Serializable
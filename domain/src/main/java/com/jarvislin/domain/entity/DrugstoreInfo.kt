package com.jarvislin.domain.entity

import androidx.room.Embedded
import java.io.Serializable

data class DrugstoreInfo(
    @Embedded
    val drugstore: Drugstore,
    @Embedded
    val openData: OpenData
) : Serializable, EntireInfo {
    override fun getId(): String = drugstore.id
    override fun getName(): String = drugstore.name
    override fun getLat(): Double = drugstore.lat
    override fun getLng(): Double = drugstore.lng
    override fun getUpdateAt(): String = openData.updateAt
    override fun getAdultMaskAmount(): Int = openData.adultMaskAmount
    override fun getChildMaskAmount(): Int = openData.childMaskAmount
    override fun getNote(): String = drugstore.note
    override fun getAvailable(): String = ""
    override fun getAddress(): String = drugstore.address
    override fun getPhone(): String = drugstore.phone
    fun toEntireInfo(): EntireInfo = this
}
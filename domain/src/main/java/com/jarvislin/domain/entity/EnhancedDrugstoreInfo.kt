package com.jarvislin.domain.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EnhancedDrugstoreInfo(
    @SerializedName("type")
    val type: String,
    @SerializedName("features")
    val features: List<Feature>
) : Serializable

data class Feature(
    @SerializedName("properties")
    val property: Property,
    @SerializedName("geometry")
    val geometry: Geometry
) : Serializable, EntireInfo {
    override fun getId(): String = property.id
    override fun getName(): String = property.name
    override fun getLat(): Double = geometry.getLat()
    override fun getLng(): Double = geometry.getLng()
    override fun getUpdateAt(): String = property.updated
    override fun getAdultMaskAmount(): Int = property.maskAdult
    override fun getChildMaskAmount(): Int = property.maskChild
    override fun getNote(): String = property.note
    override fun getAvailable(): String = property.available
    override fun getAddress(): String = property.address
    override fun getPhone(): String = property.phone
}

data class Property(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("mask_adult")
    val maskAdult: Int,
    @SerializedName("mask_child")
    val maskChild: Int,
    @SerializedName("updated")
    val updated: String,
    @SerializedName("available")
    val available: String,
    @SerializedName("note")
    val note: String,
    @SerializedName("website")
    val website: String,
    @SerializedName("county")
    val county: String,
    @SerializedName("town")
    val town: String,
    @SerializedName("cunli")
    val cunli: String
) : Serializable

data class Geometry(
    val type: String,
    val coordinates: List<Double>
) : Serializable {
    fun getLat(): Double = coordinates[1]
    fun getLng(): Double = coordinates[0]
}
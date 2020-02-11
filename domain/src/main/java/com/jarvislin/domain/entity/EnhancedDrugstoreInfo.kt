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
) : Serializable {
    fun getId(): String = property.id
    fun getName(): String = property.name
    fun getLat(): Double = geometry.getLat().toDouble()
    fun getLng(): Double = geometry.getLng().toDouble()
    fun getUpdateAt(): String = property.updated
    fun getAdultMaskAmount(): Int = property.maskAdult.toInt()
    fun getChildMaskAmount(): Int = property.maskChild.toInt()
    fun getNote(): String = property.note
    fun getAvailable(): String = property.available
    fun getAddress(): String = property.address
    fun getPhone(): String = property.phone
    fun isValid(): Boolean {
        return try {
            getLat()
            getLng()
            getAdultMaskAmount()
            getChildMaskAmount()
            true
        } catch (ex: Exception) {
            false
        }
    }
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
    val maskAdult: String, // defensive
    @SerializedName("mask_child")
    val maskChild: String, // defensive
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
    val coordinates: List<String>
) : Serializable {
    // defensive
    // because somebody put opening hours here XD
    fun getLat(): String = coordinates[1]
    fun getLng(): String = coordinates[0]
}
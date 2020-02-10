package com.jarvislin.domain.entity

import java.io.Serializable

interface EntireInfo : Serializable {
    fun getId(): String
    fun getName(): String
    fun getLat(): Double
    fun getLng(): Double
    fun getUpdateAt(): String
    fun getAdultMaskAmount(): Int
    fun getChildMaskAmount(): Int
    fun getNote(): String
    fun getAvailable(): String
    fun getPhone(): String
    fun getAddress(): String
}
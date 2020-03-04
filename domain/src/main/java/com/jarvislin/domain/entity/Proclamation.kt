package com.jarvislin.domain.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Proclamation(
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("image")
    val image: String?,
    @SerializedName("text")
    val text: String?
) : Serializable
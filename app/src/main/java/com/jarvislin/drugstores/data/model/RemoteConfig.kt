package com.jarvislin.drugstores.data.model

import com.google.gson.annotations.SerializedName
import com.jarvislin.domain.entity.Proclamation
import java.text.SimpleDateFormat
import java.util.*

class RemoteConfig(
    @SerializedName("proclamations")
    val proclamations: List<RemoteProclamation>
)

class RemoteProclamation(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("expired_at")
    val expiredAt: String,
    @SerializedName("image")
    val image: String?,
    @SerializedName("text")
    val text: String?
) {
    fun toProclamation(): Proclamation {
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        val created = try {
            format.parse(createdAt)
        } catch (ex: Exception) {
            Date()
        }
        val expired = try {
            format.parse(expiredAt)
        } catch (ex: Exception) {
            Date()
        }
        return Proclamation(
            createdAt = created,
            expiredAt = expired,
            image = image,
            text = text
        )
    }
}
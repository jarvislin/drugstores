package com.jarvislin.drugstores.data.model

import com.google.gson.annotations.SerializedName
import com.jarvislin.domain.entity.Proclamation

class RemoteConfig(
    @SerializedName("proclamations")
    val proclamations: List<Proclamation>
)
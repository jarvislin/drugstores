package com.jarvislin.domain.entity

import androidx.room.Embedded
import java.io.Serializable

data class DrugstoreInfo(
    @Embedded
    val drugstore: Drugstore,
    @Embedded
    val openData: OpenData
) : Serializable
package com.jarvislin.domain.entity

import androidx.room.Embedded

data class DrugstoreInfo(
    @Embedded
    val drugstore: Drugstore,
    @Embedded
    val openData: OpenData
)
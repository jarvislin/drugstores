package com.jarvislin.domain.repository

import com.jarvislin.domain.entity.RapidTestLocation
import io.reactivex.Single
import java.io.File

interface RapidTestRepository {
    fun downloadData(): Single<File>
    fun convert(file: File): Single<List<RapidTestLocation>>
}
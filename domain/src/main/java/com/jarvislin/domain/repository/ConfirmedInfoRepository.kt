package com.jarvislin.domain.repository

import com.jarvislin.domain.entity.Dashboard
import com.jarvislin.domain.entity.DownloadResult
import io.reactivex.Single
import java.io.File

interface ConfirmedInfoRepository {
    fun downloadConfirmedCase():Single<DownloadResult>
    fun convertFile(file: File):Single<Dashboard>
}
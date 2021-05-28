package com.jarvislin.drugstores.repository

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.jarvislin.domain.entity.ConfirmedCase
import com.jarvislin.domain.entity.ContentNotFoundException
import com.jarvislin.domain.entity.DownloadResult
import com.jarvislin.domain.entity.HeaderNotFoundException
import com.jarvislin.domain.repository.ConfirmedInfoRepository
import com.jarvislin.drugstores.data.remote.Downloader
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File

class ConfirmedInfoRepositoryImpl(private val downloader: Downloader) : ConfirmedInfoRepository {
    override fun downloadConfirmedCase(): Single<DownloadResult> {
        return downloader.download(CSV_URL)
            .subscribeOn(Schedulers.io())
    }

    override fun convertFile(file: File): Single<ConfirmedCase> {
        return Single.just(file)
            .map { csvReader().open(it) { readAllWithHeader() } }
            .map { if (it.isEmpty()) throw ContentNotFoundException() else it.first() }
            .map {
                if (it.containsKey("確診") && it.containsKey("送驗") && it.containsKey("排除"))
                    ConfirmedCase(it["確診"]!!, it["送驗"]!!, it["排除"]!!)
                else throw HeaderNotFoundException()
            }
            .subscribeOn(Schedulers.io())
    }

    companion object {
        private const val CSV_URL =
            "https://od.cdc.gov.tw/eic/covid19/covid19_tw_stats.csv"
    }
}
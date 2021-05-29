package com.jarvislin.drugstores.repository

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.jarvislin.domain.entity.Dashboard
import com.jarvislin.domain.entity.ContentNotFoundException
import com.jarvislin.domain.entity.DownloadResult
import com.jarvislin.domain.entity.HeaderNotFoundException
import com.jarvislin.domain.repository.ConfirmedInfoRepository
import com.jarvislin.drugstores.data.remote.Downloader
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.text.NumberFormat

class ConfirmedInfoRepositoryImpl(private val downloader: Downloader) : ConfirmedInfoRepository {
    override fun downloadConfirmedCase(): Single<DownloadResult> {
        return downloader.download(CSV_URL)
            .subscribeOn(Schedulers.io())
    }

    override fun convertFile(file: File): Single<Dashboard> {
        return Single.just(file)
            .map { csvReader().open(it) { readAllWithHeader() } }
            .map { if (it.isEmpty()) throw ContentNotFoundException() else it.first() }
            .map {
                if (it.containsKey("確診") && it.containsKey("送驗") && it.containsKey("排除") &&
                    it.containsKey("昨日排除") && it.containsKey("昨日送驗") &&
                    it.containsKey("解除隔離") && it.containsKey("死亡") && it.containsKey("昨日確診")
                )
                    Dashboard(
                        confirmedCount = getFormattedNumber(it["確診"]!!),
                        testingCount = getFormattedNumber(it["送驗"]!!),
                        excludedCount = getFormattedNumber(it["排除"]!!),
                        yesterdayConfirmedCount = getFormattedNumber(it["昨日確診"]!!),
                        yesterdayTestingCount = getFormattedNumber(it["昨日送驗"]!!),
                        yesterdayExcludedCount = getFormattedNumber(it["昨日排除"]!!),
                        deathCount = getFormattedNumber(it["死亡"]!!),
                        recoveredCount = getFormattedNumber(it["解除隔離"]!!)
                    )
                else throw HeaderNotFoundException()
            }
            .subscribeOn(Schedulers.io())
    }

    private fun getFormattedNumber(text: String): String {
        return if (text.isNumber()) {
            NumberFormat.getInstance().format(text.toInt())
        } else {
            text
        }
    }

    fun String.isNumber(): Boolean {
        return this.toCharArray().all { it.isDigit() }
    }

    companion object {
        private const val CSV_URL =
            "https://od.cdc.gov.tw/eic/covid19/covid19_tw_stats.csv"
    }
}
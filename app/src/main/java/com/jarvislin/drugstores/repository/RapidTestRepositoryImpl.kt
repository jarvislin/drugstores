package com.jarvislin.drugstores.repository

import android.webkit.URLUtil
import com.jarvislin.domain.entity.RapidTestLocation
import com.jarvislin.domain.repository.RapidTestRepository
import com.jarvislin.drugstores.data.model.ApiRapidTestLocation
import com.jarvislin.drugstores.data.remote.Downloader
import com.jarvislin.drugstores.extension.toObject
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.lang.Exception

class RapidTestRepositoryImpl(private val downloader: Downloader) : RapidTestRepository {
    override fun downloadData(): Single<File> {
        return downloader.download(JSON_URL)
            .map { it.file }
            .subscribeOn(Schedulers.io())
    }

    override fun convert(file: File): Single<List<RapidTestLocation>> {
        return Single.just(file)
            .map { it.readText() }
            .map { it.toObject(ApiRapidTestLocation::class.java) }
            .map { it.features }
            .map { it.map { it.property } }
            .map {
                it.map { data ->
                    RapidTestLocation(
                        city = data.city,
                        name = extractText(data.name),
                        suspended = data.suspended != "FALSE",
                        unchecked = data.unchecked != "FALSE",
                        updatedTime = extractText(data.updatedTime),
                        dataSourceUrl = extractUrl(data.source),
                        hospitalName = extractText(data.hospitalName),
                        limit = extractText(data.limit),
                        quotaOfPeople = extractText(data.quotaOfPeople),
                        openingHours = extractText(data.openingHours),
                        method = extractText(data.method),
                        reservationUrl = extractUrl(data.reservationWebsite),
                        locationDescription = if (data.address == data.areaDescription) null
                        else extractText(data.areaDescription),
                        address = extractText(data.address),
                        phone = extractText(data.phone),
                        note = extractText(data.note),
                        latitude = extractDouble(data.latitude),
                        longitude = extractDouble(data.longitude),
                    )
                }
            }
            .subscribeOn(Schedulers.io())
    }

    private fun extractText(text: String): String? {
        return if (text.isEmpty()) {
            null
        } else {
            text
        }
    }

    private fun extractDouble(text: String): Double? {
        return try {
            text.toDouble()
        } catch (ex: Exception) {
            null
        }
    }

    private fun extractUrl(text: String): String? {
        return if (URLUtil.isValidUrl(text)) {
            text
        } else {
            null
        }
    }

    companion object {
        private const val JSON_URL =
            "https://raw.githubusercontent.com/kiang/nidss.cdc.gov.tw/master/data/points.json"
    }
}
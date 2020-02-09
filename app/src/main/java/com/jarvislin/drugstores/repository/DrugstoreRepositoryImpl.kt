package com.jarvislin.drugstores.repository

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.jarvislin.domain.entity.Drugstore
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.OpenData
import com.jarvislin.domain.entity.Progress
import com.jarvislin.domain.repository.DrugstoreRepository
import com.jarvislin.drugstores.page.map.MarkerCacheManager.Companion.MAX_MARKER_AMOUNT
import com.jarvislin.drugstores.base.App
import com.jarvislin.drugstores.data.LocalData
import com.jarvislin.drugstores.data.db.DrugstoreDao
import com.jarvislin.drugstores.data.remote.Downloader
import com.jarvislin.drugstores.extension.toList
import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.nio.charset.Charset


class DrugstoreRepositoryImpl(
    private val drugstoreDao: DrugstoreDao,
    private val localData: LocalData
) : DrugstoreRepository {
    override fun transformOpenData(file: File): Single<List<OpenData>> {
        return Single.create<List<OpenData>> { emitter ->
            try {
                csvReader().readAllWithHeader(file).map {
                    OpenData(
                        id = it["醫事機構代碼"] ?: error("wrong key"),
                        adultMaskAmount = it["成人口罩總剩餘數"]?.toInt() ?: error("wrong key"),
                        childMaskAmount = it["兒童口罩剩餘數"]?.toInt() ?: error("wrong key"),
                        updateAt = it["來源資料時間"] ?: error("wrong key")
                    )
                }.let { emitter.onSuccess(it) }
            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun saveOpenData(data: List<OpenData>): Completable {
        return drugstoreDao.insertOpenData(data)
    }

    override fun deleteOpenData(): Single<Int> {
        return drugstoreDao.deleteOpenData()
            .subscribeOn(Schedulers.io())
    }

    override fun saveDrugstores(stores: List<Drugstore>): Completable {
        return drugstoreDao.insertDrugstores(stores)
            .subscribeOn(Schedulers.io())
    }

    override fun initDrugstores(): Single<List<Drugstore>> {
        return Single.create<List<Drugstore>> { emitter ->
            try {
                App.instance().assets.open("info.json").use { stream ->
                    val size: Int = stream.available()
                    val buffer = ByteArray(size)
                    stream.read(buffer)
                    emitter.onSuccess(String(buffer, Charset.forName("UTF-8")).toList())
                }
            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun findNearDrugstoreInfo(
        latitude: Double,
        longitude: Double
    ): Single<List<DrugstoreInfo>> {
        return drugstoreDao.findNearDrugstoreInfo(latitude, longitude, MAX_MARKER_AMOUNT)
            .subscribeOn(Schedulers.io())
    }

    override fun saveLastLocation(latitude: Double, longitude: Double) {
        localData.lastLocation = "$latitude,$longitude"
    }

    override fun getLastLocation(): Pair<Double, Double> {
        val location = localData.lastLocation.split(",").map { it.toDouble() }
        return Pair(location[0], location[1])
    }

    override fun downloadOpenData(): Flowable<Progress> {
        return Downloader().download("https://raw.githubusercontent.com/kiang/pharmacies/master/raw/maskdata.csv")
            .toFlowable(BackpressureStrategy.LATEST)
            .subscribeOn(Schedulers.io())
    }

    override fun searchAddress(keyword: String): Single<List<DrugstoreInfo>> {
        return drugstoreDao.searchAddress(keyword)
            .subscribeOn(Schedulers.io())
    }
}
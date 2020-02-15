package com.jarvislin.drugstores.repository

import com.jarvislin.domain.entity.*
import com.jarvislin.domain.repository.DrugstoreRepository
import com.jarvislin.drugstores.data.LocalData
import com.jarvislin.drugstores.data.db.DrugstoreDao
import com.jarvislin.drugstores.data.remote.Downloader
import com.jarvislin.drugstores.extension.toObject
import com.jarvislin.drugstores.page.map.MarkerCacheManager.Companion.MAX_MARKER_AMOUNT
import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset


class DrugstoreRepositoryImpl(
    private val drugstoreDao: DrugstoreDao,
    private val localData: LocalData,
    private val downloader: Downloader
) : DrugstoreRepository {

    companion object {
        private const val DATA_URL = "https://raw.githubusercontent.com/kiang/pharmacies/master/json/points.json"
    }

    override fun saveDrugstoreInfo(data: List<DrugstoreInfo>): Completable {
        return drugstoreDao.addDrugstoreInfo(data)
            .subscribeOn(Schedulers.io())
    }

    override fun deleteDrugstoreInfo(): Single<Int> {
        return drugstoreDao.removeDrugstoreInfo()
            .subscribeOn(Schedulers.io())
    }

    override fun findNearDrugstoreInfo(
        latitude: Double,
        longitude: Double
    ): Single<List<DrugstoreInfo>> {
        return drugstoreDao.getNearDrugstoreInfo(latitude, longitude, MAX_MARKER_AMOUNT)
            .subscribeOn(Schedulers.io())
    }

    override fun saveLastLocation(latitude: Double, longitude: Double) {
        localData.lastLocation = "$latitude,$longitude"
    }

    override fun getLastLocation(): Pair<Double, Double> {
        val location = localData.lastLocation.split(",").map { it.toDouble() }
        return Pair(location[0], location[1])
    }

    override fun downloadData(): Flowable<Progress> {
        return downloader.download(DATA_URL)
            .toFlowable(BackpressureStrategy.LATEST)
            .subscribeOn(Schedulers.io())
    }

    override fun searchAddress(keyword: String): Single<List<DrugstoreInfo>> {
        return drugstoreDao.getSearchResult(keyword)
            .subscribeOn(Schedulers.io())
    }

    override fun transformToDrugstoreInfo(file: File): Single<List<DrugstoreInfo>> {
        return Single.create<List<DrugstoreInfo>> { emitter ->
            val stream = FileInputStream(file)
            try {
                val channel: FileChannel = stream.channel
                val buffer: MappedByteBuffer =
                    channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                val json = Charset.defaultCharset().decode(buffer).toString()
                val info = json.toObject(ApiDrugstoreInfo::class.java)
                info.features.filter { it.isValid() }
                    .map {
                        DrugstoreInfo(
                            id = it.getId(),
                            name = it.getName(),
                            lat = it.getLat(),
                            lng = it.getLng(),
                            address = it.getAddress(),
                            phone = it.getPhone(),
                            note = it.getNote(),
                            adultMaskAmount = it.getAdultMaskAmount(),
                            childMaskAmount = it.getChildMaskAmount(),
                            updateAt = it.getUpdateAt(),
                            available = it.getAvailable()
                        )
                    }.let { emitter.onSuccess(it) }
            } catch (ex: Exception) {
                emitter.onError(ex)
            } finally {
                stream.close()
            }
        }.subscribeOn(Schedulers.io())
    }
}
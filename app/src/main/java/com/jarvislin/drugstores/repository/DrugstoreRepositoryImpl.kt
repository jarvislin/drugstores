package com.jarvislin.drugstores.repository

import com.jarvislin.domain.entity.*
import com.jarvislin.domain.repository.DrugstoreRepository
import com.jarvislin.drugstores.base.App
import com.jarvislin.drugstores.data.LocalData
import com.jarvislin.drugstores.data.db.DrugstoreDao
import com.jarvislin.drugstores.data.remote.Downloader
import com.jarvislin.drugstores.extension.toList
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


    override fun saveOpenData(data: List<OpenData>): Completable {
        return drugstoreDao.insertOpenData(data)
            .subscribeOn(Schedulers.io())
    }

    override fun deleteOpenData(): Single<Int> {
        return drugstoreDao.deleteOpenData()
            .subscribeOn(Schedulers.io())
    }

    override fun deleteDrugstores(): Single<Int> {
        return drugstoreDao.deleteDrugstores()
            .subscribeOn(Schedulers.io())
    }

    override fun saveDrugstores(stores: List<Drugstore>) {
        return drugstoreDao.insertDrugstores(stores)
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
    ): Single<List<EntireInfo>> {
        return drugstoreDao.findNearDrugstoreInfo(latitude, longitude, MAX_MARKER_AMOUNT)
            .map { it.map { it.toEntireInfo() } }
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
        return downloader.download("https://raw.githubusercontent.com/kiang/pharmacies/master/json/points.json")
            .toFlowable(BackpressureStrategy.LATEST)
            .subscribeOn(Schedulers.io())
    }

    override fun searchAddress(keyword: String): Single<List<EntireInfo>> {
        return drugstoreDao.searchAddress(keyword)
            .map { it.map { it.toEntireInfo() } }
            .subscribeOn(Schedulers.io())
    }

    override fun transformToDrugstores(file: File): Single<List<Drugstore>> {
        return Single.create<List<Drugstore>> { emitter ->
            val stream = FileInputStream(file)
            try {
                val channel: FileChannel = stream.channel
                val buffer: MappedByteBuffer =
                    channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                val json = Charset.defaultCharset().decode(buffer).toString()
                val info = json.toObject(EnhancedDrugstoreInfo::class.java)
                info.features.map {
                    Drugstore(
                        id = it.getId(),
                        name = it.getName(),
                        lat = it.getLat(),
                        lng = it.getLng(),
                        address = it.getAddress(),
                        phone = it.getPhone(),
                        note = it.getNote()
                    )
                }.let { emitter.onSuccess(it) }
            } catch (ex: Exception) {
                emitter.onError(ex)
            } finally {
                stream.close()
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun transformToOpenData(file: File): Single<List<OpenData>> {
        return Single.create<List<OpenData>> { emitter ->
            val stream = FileInputStream(file)
            try {
                val channel: FileChannel = stream.channel
                val buffer: MappedByteBuffer =
                    channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                val json = Charset.defaultCharset().decode(buffer).toString()
                val info = json.toObject(EnhancedDrugstoreInfo::class.java)
                info.features.map {
                    OpenData(
                        id = it.getId(),
                        adultMaskAmount = it.getAdultMaskAmount(),
                        childMaskAmount = it.getChildMaskAmount(),
                        updateAt = it.getUpdateAt()
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
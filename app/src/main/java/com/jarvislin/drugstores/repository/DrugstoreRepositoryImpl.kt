package com.jarvislin.drugstores.repository

import android.text.format.DateUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jarvislin.domain.entity.*
import com.jarvislin.domain.repository.DrugstoreRepository
import com.jarvislin.drugstores.data.LocalData
import com.jarvislin.drugstores.data.db.DrugstoreDao
import com.jarvislin.drugstores.data.model.ApiDrugstoreInfo
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
    private val downloader: Downloader,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : DrugstoreRepository {


    companion object {
        private const val DATA_URL =
            "https://raw.githubusercontent.com/kiang/pharmacies/master/json/points.json"
        private const val COLLECTION_ROOT = "drugstores"
        private const val COLLECTION_REPORTS = "reports"
        private const val COLLECTION_HISTORIES = "histories"
        private const val FILED_STATUS = "status"
        private const val FILED_TIMESTAMP = "timestamp"
        private const val FILED_USES_NUMBER_TICKET = "uses_number_ticket"
        private const val FILED_MASK_ADULT = "mask_adult"
        private const val FILED_MASK_CHILD = "mask_child"
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
                            available = it.getServicePeriods()
                        )
                    }.let { emitter.onSuccess(it) }
            } catch (ex: Exception) {
                emitter.onError(ex)
            } finally {
                stream.close()
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun reportMaskStatus(id: String, status: Status): Completable {
        val data = hashMapOf(
            FILED_STATUS to status.value,
            FILED_TIMESTAMP to Timestamp.now()
        )

        return Completable.create { emitter ->
            db.collection(COLLECTION_ROOT)
                .document(id)
                .collection(COLLECTION_REPORTS)
                .add(data)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
        }.subscribeOn(Schedulers.io())
    }

    override fun fetchMaskStatus(id: String): Maybe<MaskStatus> {
        return Maybe.create<MaskStatus> { emitter ->
            db.collection(COLLECTION_ROOT)
                .document(id)
                .collection(COLLECTION_REPORTS)
                .orderBy(FILED_TIMESTAMP, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener {
                    if (it.documents.isEmpty()) {
                        emitter.onComplete()
                        return@addOnSuccessListener
                    }

                    it.documents.first().apply {
                        val status = getLong(FILED_STATUS).let { Status.from(it) }
                        val timestamp = getDate(FILED_TIMESTAMP)
                        if (status == null || timestamp == null) {
                            emitter.onComplete()
                        } else {
                            if (DateUtils.isToday(timestamp.time)) {
                                emitter.onSuccess(MaskStatus(status, timestamp))
                            } else {
                                emitter.onComplete()
                            }
                        }
                    }

                }
                .addOnFailureListener { emitter.onError(it) }
        }.subscribeOn(Schedulers.io())
    }

    override fun reportNumberTicket(id: String, isNumberTicket: Boolean): Completable {
        val data = hashMapOf(
            FILED_USES_NUMBER_TICKET to isNumberTicket
        )

        return Completable.create { emitter ->
            db.collection(COLLECTION_ROOT)
                .document(id)
                .set(data)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun fetchUsesNumberTicket(id: String): Maybe<Boolean> {
        return Maybe.create<Boolean> { emitter ->
            db.collection(COLLECTION_ROOT)
                .document(id)
                .get()
                .addOnSuccessListener {
                    val usesNumberTicket = it.getBoolean(FILED_USES_NUMBER_TICKET)
                    if (usesNumberTicket == null) {
                        emitter.onComplete()
                    } else {
                        emitter.onSuccess(usesNumberTicket)
                    }
                }
                .addOnFailureListener { emitter.onError(it) }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun fetchRecords(id: String): Maybe<List<MaskRecord>> {
        return Maybe.create<List<MaskRecord>> { emitter ->
            db.collection(COLLECTION_ROOT)
                .document(id)
                .collection(COLLECTION_HISTORIES)
                .orderBy(FILED_TIMESTAMP, Query.Direction.DESCENDING)
                .limit(31)
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        emitter.onComplete()
                    } else {
                        it.documents.filter {
                            it.getLong(FILED_MASK_ADULT) != null &&
                                    it.getLong(FILED_MASK_CHILD) != null &&
                                    it.getDate(FILED_TIMESTAMP) != null
                        }.map {
                            MaskRecord(
                                it.getLong(FILED_MASK_ADULT)!!.toInt(),
                                it.getLong(FILED_MASK_CHILD)!!.toInt(),
                                it.getDate(FILED_TIMESTAMP)!!
                            )
                        }.let {
                            if (it.isEmpty()) {
                                emitter.onComplete()
                            } else {
                                emitter.onSuccess(it.sortedBy { it.date })
                            }
                        }
                    }
                }
                .addOnFailureListener { emitter.onError(it) }
        }.subscribeOn(Schedulers.io())
    }

    override fun isValidReportTime(): Boolean {
        return System.currentTimeMillis() - localData.lastReportTimestamp > 1000 * 60 * 10 // 10 mins
    }

    override fun saveReportTime() {
        localData.lastReportTimestamp = System.currentTimeMillis()
    }

    override fun isFirstLaunch() = localData.isFirstLaunch

    override fun updateFirstLaunch() {
        localData.isFirstLaunch = false
    }

    override fun updateRatingCount() {
        localData.ratingCount = localData.ratingCount + 1
    }
}
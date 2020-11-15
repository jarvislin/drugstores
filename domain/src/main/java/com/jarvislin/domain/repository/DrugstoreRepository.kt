package com.jarvislin.domain.repository

import com.jarvislin.domain.entity.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import java.io.File

interface DrugstoreRepository {
    fun deleteDrugstoreInfo(): Single<Int>
    fun saveDrugstoreInfo(data: List<DrugstoreInfo>): Completable
    fun findNearDrugstoreInfo(latitude: Double, longitude: Double): Single<List<DrugstoreInfo>>
    fun saveLastLocation(latitude: Double, longitude: Double)
    fun getLastLocation(): Pair<Double, Double>
    fun downloadData(): Single<DownloadResult>
    fun transformToDrugstoreInfo(file: File): Single<List<DrugstoreInfo>>
    fun searchAddress(keyword: String): Single<List<DrugstoreInfo>>
    fun reportMaskStatus(id: String, status: Status): Completable
    fun fetchMaskStatus(id: String): Maybe<MaskStatus>
    fun isValidReportTime(): Boolean
    fun saveReportTime()
    fun reportNumberTicket(id: String, isNumberTicket: Boolean): Completable
    fun fetchUsesNumberTicket(id: String): Maybe<Boolean>
    fun fetchRecords(id: String): Maybe<List<MaskRecord>>
    fun isFirstLaunch(): Boolean
    fun updateFirstLaunch()
    fun updateRatingCount()
}
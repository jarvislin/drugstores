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
    fun downloadData(): Flowable<Progress>
    fun transformToDrugstoreInfo(file: File): Single<List<DrugstoreInfo>>
    fun searchAddress(keyword: String): Single<List<DrugstoreInfo>>
    fun reportMaskStatus(id: String, status: Status): Completable
    fun fetchMaskStatus(id: String): Maybe<MaskStatus>
}
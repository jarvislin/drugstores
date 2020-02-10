package com.jarvislin.domain.repository

import com.jarvislin.domain.entity.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.io.File

interface DrugstoreRepository {
    fun deleteOpenData(): Single<Int>
    fun deleteDrugstores(): Single<Int>
    fun saveOpenData(data: List<OpenData>): Completable
    fun saveDrugstores(stores: List<Drugstore>)
    fun initDrugstores(): Single<List<Drugstore>>
    fun findNearDrugstoreInfo(latitude: Double, longitude: Double): Single<List<EntireInfo>>
    fun saveLastLocation(latitude: Double, longitude: Double)
    fun getLastLocation(): Pair<Double, Double>
    fun downloadOpenData():Flowable<Progress>
    fun transformToOpenData(file: File): Single<List<OpenData>>
    fun searchAddress(keyword: String): Single<List<EntireInfo>>
    fun transformToDrugstores(file: File): Single<List<Drugstore>>
}
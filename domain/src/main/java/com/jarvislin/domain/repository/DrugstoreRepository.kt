package com.jarvislin.domain.repository

import com.jarvislin.domain.entity.Drugstore
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.OpenData
import com.jarvislin.domain.entity.Progress
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.io.File

interface DrugstoreRepository {
    fun fetchOpenData(): Single<List<OpenData>>
    fun saveOpenData(data: List<OpenData>): Completable
    fun deleteOpenData(): Single<Int>
    fun saveDrugstores(stores: List<Drugstore>): Completable
    fun initDrugstores(): Single<List<Drugstore>>
    fun findNearDrugstoreInfo(latitude: Double, longitude: Double): Single<List<DrugstoreInfo>>
    fun saveLastLocation(latitude: Double, longitude: Double)
    fun getLastLocation(): Pair<Double, Double>
    fun downloadOpenData():Flowable<Progress>
    fun transformOpenData(file: File): Single<List<OpenData>>
}
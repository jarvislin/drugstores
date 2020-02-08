package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.Progress
import com.jarvislin.domain.repository.DrugstoreRepository
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.io.File

class DrugstoreUseCase(private val drugstoreRepository: DrugstoreRepository) {

    fun fetchOpenData(): Flowable<Progress> {
        return drugstoreRepository.downloadOpenData()
    }

    fun initDrugstores(): Completable {
        return drugstoreRepository.initDrugstores()
            .flatMapCompletable { drugstoreRepository.saveDrugstores(it) }
    }

    fun findNearDrugstoreInfo(latitude: Double, longitude: Double): Single<List<DrugstoreInfo>> {
        return drugstoreRepository.findNearDrugstoreInfo(latitude, longitude)
    }

    fun saveLocation(latitude: Double, longitude: Double) {
        drugstoreRepository.saveLastLocation(latitude, longitude)
    }

    fun getLastLocation(): Pair<Double, Double> {
        return drugstoreRepository.getLastLocation()
    }

    fun handleLatestOpenData(file: File): Completable {
        return drugstoreRepository.deleteOpenData()
            .flatMap { drugstoreRepository.transformOpenData(file) }
            .flatMapCompletable { drugstoreRepository.saveOpenData(it) }
    }
}
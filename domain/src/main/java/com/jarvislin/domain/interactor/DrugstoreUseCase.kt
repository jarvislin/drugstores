package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.EntireInfo
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

    fun findNearDrugstoreInfo(latitude: Double, longitude: Double): Single<List<EntireInfo>> {
        return drugstoreRepository.findNearDrugstoreInfo(latitude, longitude)
    }

    fun saveLocation(latitude: Double, longitude: Double) {
        drugstoreRepository.saveLastLocation(latitude, longitude)
    }

    fun getLastLocation(): Pair<Double, Double> {
        return drugstoreRepository.getLastLocation()
    }

    fun handleLatestOpenData(file: File): Completable {
        return drugstoreRepository.deleteDrugstores()
            .flatMap { drugstoreRepository.transformToDrugstores(file) }
            .map { drugstoreRepository.saveDrugstores(it) }
            .flatMap { drugstoreRepository.deleteOpenData() }
            .flatMap { drugstoreRepository.transformToOpenData(file) }
            .flatMapCompletable { drugstoreRepository.saveOpenData(it) }
    }

    fun searchAddress(keyword: String): Single<List<EntireInfo>> {
        return drugstoreRepository.searchAddress(keyword)
    }
}
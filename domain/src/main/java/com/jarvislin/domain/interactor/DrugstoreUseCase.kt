package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.MaskStatus
import com.jarvislin.domain.entity.Progress
import com.jarvislin.domain.entity.Status
import com.jarvislin.domain.repository.DrugstoreRepository
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import java.io.File

class DrugstoreUseCase(private val drugstoreRepository: DrugstoreRepository) {

    fun fetchData(): Flowable<Progress> {
        return drugstoreRepository.downloadData()
    }

    fun findNearDrugstoreInfo(latitude: Double, longitude: Double): Single<List<DrugstoreInfo>> {
        return drugstoreRepository.findNearDrugstoreInfo(latitude, longitude)
    }

    fun saveLastLocation(latitude: Double, longitude: Double) {
        drugstoreRepository.saveLastLocation(latitude, longitude)
    }

    fun getLastLocation(): Pair<Double, Double> {
        return drugstoreRepository.getLastLocation()
    }

    fun handleLatestData(file: File): Completable {
        return drugstoreRepository.deleteDrugstoreInfo()
            .flatMap { drugstoreRepository.transformToDrugstoreInfo(file) }
            .flatMapCompletable { drugstoreRepository.saveDrugstoreInfo(it) }
    }

    fun searchAddress(keyword: String): Single<List<DrugstoreInfo>> {
        return drugstoreRepository.searchAddress(keyword)
    }

    fun reportMaskStatus(id: String, status: Status): Completable {
        return drugstoreRepository.reportMaskStatus(id, status)
    }

    fun fetchMaskStatus(id: String): Maybe<MaskStatus> {
        return drugstoreRepository.fetchMaskStatus(id)
    }
}
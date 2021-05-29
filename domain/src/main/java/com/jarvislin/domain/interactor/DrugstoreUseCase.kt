package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.*
import com.jarvislin.domain.repository.DrugstoreRepository
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.io.File

class DrugstoreUseCase(private val drugstoreRepository: DrugstoreRepository) {

    fun findNearDrugstoreInfo(latitude: Double, longitude: Double): Single<List<DrugstoreInfo>> {
        return drugstoreRepository.findNearDrugstoreInfo(latitude, longitude)
    }

    fun saveLastLocation(latitude: Double, longitude: Double) {
        drugstoreRepository.saveLastLocation(latitude, longitude)
    }

    fun getLastLocation(): Pair<Double, Double> {
        return drugstoreRepository.getLastLocation()
    }

    fun fetchData(subject: PublishSubject<UpdateProgress>): Completable {
        var file: File? = null
        return drugstoreRepository.downloadData()
            .doOnSubscribe { subject.onNext(StartDownloading) }
            .doOnSuccess {
                subject.onNext(LatestDataDownloaded)
                file = it.file
            }
            .flatMap { drugstoreRepository.deleteDrugstoreInfo() }
            .doOnSuccess { subject.onNext(OldDataDeleted) }
            .flatMap { drugstoreRepository.transformToDrugstoreInfo(file!!) }
            .doOnSuccess { subject.onNext(LatestDataTransformed) }
            .flatMapCompletable { drugstoreRepository.saveDrugstoreInfo(it) }
            .doOnComplete { subject.onNext(LatestDataSaved) }
            .doOnError { subject.onNext(UpdateFailed) }
    }

    fun searchAddress(keyword: String): Single<List<DrugstoreInfo>> {
        return drugstoreRepository.searchAddress(keyword)
    }

    fun isFirstLaunch() = drugstoreRepository.isFirstLaunch()
    fun updateFirstLaunch() {
        drugstoreRepository.updateFirstLaunch()
    }

    fun updateRatingCount() {
        drugstoreRepository.updateRatingCount()
    }
}
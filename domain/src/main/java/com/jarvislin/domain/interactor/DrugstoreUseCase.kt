package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.Drugstore
import com.jarvislin.domain.repository.DrugstoreRepository
import io.reactivex.Completable
import io.reactivex.Single

class DrugstoreUseCase(private val drugstoreRepository: DrugstoreRepository) {

    fun fetchOpenData(): Completable {
        return drugstoreRepository.deleteOpenData()
            .flatMap { drugstoreRepository.fetchOpenData() }
            .flatMapCompletable { drugstoreRepository.saveOpenData(it) }
    }

    fun initDrugstores(): Completable {
        return drugstoreRepository.initDrugstores()
            .flatMapCompletable { drugstoreRepository.insertDrugstores(it) }
    }

    fun fetchNearStores(latitude: Double, longitude: Double): Single<List<Drugstore>> {
        return drugstoreRepository.fetchNearStores(latitude, longitude)
    }
}
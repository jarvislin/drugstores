package com.jarvislin.domain.repository

import com.jarvislin.domain.entity.Drugstore
import com.jarvislin.domain.entity.OpenData
import io.reactivex.Completable
import io.reactivex.Single

interface DrugstoreRepository {
    fun fetchOpenData(): Single<List<OpenData>>
    fun saveOpenData(data: List<OpenData>): Completable
    fun deleteOpenData(): Single<Int>
    fun insertDrugstores(stores: List<Drugstore>): Completable
    fun initDrugstores(): Single<List<Drugstore>>
    fun fetchNearStores(latitude: Double, longitude: Double): Single<List<Drugstore>>
}
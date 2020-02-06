package com.jarvislin.drugstores

import androidx.lifecycle.MutableLiveData
import com.jarvislin.domain.entity.Drugstore
import com.jarvislin.domain.interactor.DrugstoreUseCase
import com.jarvislin.drugstores.extension.bind
import com.jarvislin.petme.base.BaseViewModel
import org.koin.core.inject
import timber.log.Timber

class MapViewModel : BaseViewModel() {
    val isInitialized = MutableLiveData<Boolean>()
    val stores =
        MutableLiveData<List<Drugstore>>()

    init {
        isInitialized.value = false
        stores.value = emptyList()
    }

    fun fetchOpenData() {
        useCase.fetchOpenData()
            .subscribe({}, {})
            .bind(this)
    }

    fun initData() {
        useCase.initDrugstores()
            .subscribe({ isInitialized.postValue(true) }, {
                Timber.e(
                    it
                )
            })
            .bind(this)
    }

    fun fetchNearStores(latitude: Double, longitude: Double) {
        useCase.fetchNearStores(latitude, longitude)
            .subscribe({ stores.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }

    private val useCase: DrugstoreUseCase by inject()
}
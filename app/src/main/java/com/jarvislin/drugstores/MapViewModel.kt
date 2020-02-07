package com.jarvislin.drugstores

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.jarvislin.domain.entity.Drugstore
import com.jarvislin.domain.interactor.DrugstoreUseCase
import com.jarvislin.drugstores.extension.bind
import com.jarvislin.drugstores.base.BaseViewModel
import org.koin.core.inject
import timber.log.Timber

class MapViewModel : BaseViewModel() {
    val isInitialized = MutableLiveData<Boolean>()
    val stores = MutableLiveData<List<Drugstore>>()
    val newLocation = MutableLiveData<LatLng>()

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

    fun fetchNearDrugstoreInfo(latitude: Double, longitude: Double) {
        useCase.fetchNearDrugstoreInfo(latitude, longitude)
            .subscribe({ stores.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }

    fun saveLastLocation(location: Location?) {
        location?.let {
            useCase.saveLocation(it.latitude, it.longitude)
        }
    }

    fun getLastLocation(): LatLng {
        return useCase.getLastLocation().run {
            LatLng(first, second)
        }
    }

    private val useCase: DrugstoreUseCase by inject()
}
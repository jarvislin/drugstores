package com.jarvislin.drugstores.page.map

import android.location.Location
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.Progress
import com.jarvislin.domain.interactor.DrugstoreUseCase
import com.jarvislin.drugstores.extension.bind
import com.jarvislin.drugstores.base.BaseViewModel
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.inject
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

class MapViewModel : BaseViewModel() {
    private val useCase: DrugstoreUseCase by inject()
    private val initialized = MutableLiveData<Boolean>()
    private val downloaded = MutableLiveData<Boolean>()
    val drugstoreInfo = MutableLiveData<List<DrugstoreInfo>>()
    val downloadProgress = MutableLiveData<Progress>()
    val allDataPrepared = MediatorLiveData<Boolean>()
    val autoUpdate = MutableLiveData<Boolean>()
    val searchedResult = MutableLiveData<List<DrugstoreInfo>>()
    val statusBarHeight = MutableLiveData<Int>()

    init {
        initialized.value = false
        downloaded.value = false
        allDataPrepared.addSource(initialized) { allDataPrepared.postValue(it && downloaded.value == true) }
        allDataPrepared.addSource(downloaded) { allDataPrepared.postValue(it && initialized.value == true) }
        drugstoreInfo.value = emptyList()
    }

    fun fetchOpenData() {
        useCase.fetchOpenData()
            .subscribe({ downloadProgress.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }

    fun initDrugstores() {
        useCase.initDrugstores()
            .subscribe({ initialized.postValue(true) }, { Timber.e(it) })
            .bind(this)
    }

    fun fetchNearDrugstoreInfo(latitude: Double, longitude: Double) {
        if (allDataPrepared.value != true) {
            // from on camera idled
            return
        }
        useCase.findNearDrugstoreInfo(latitude, longitude)
            .subscribe({ drugstoreInfo.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }

    fun saveLastLocation(location: Location?) {
        location?.let { useCase.saveLocation(it.latitude, it.longitude) }
    }

    fun getLastLocation(): LatLng {
        return useCase.getLastLocation().run { LatLng(first, second) }
    }

    fun handleLatestOpenData(file: File) {
        useCase.handleLatestOpenData(file)
            .subscribe({ downloaded.postValue(true) }, { Timber.e(it) })
            .bind(this)
    }

    fun countDown() {
        Flowable.interval(1, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.computation())
            .take(120)
            .doOnComplete {
                Timber.i("count down complete")
                autoUpdate.postValue(true)
            }
            .subscribe({ }, { Timber.e(it) })
            .bind(this)
    }

    fun searchAddress(keyword: String) {
        useCase.searchAddress(keyword)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ searchedResult.value = it }, { Timber.e(it) })
            .bind(this)
    }

    fun saveStatusBarHeight(height: Int) {
        statusBarHeight.postValue(height)
    }
}
package com.jarvislin.drugstores.page.map

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.LatestDataSaved
import com.jarvislin.domain.entity.Proclamation
import com.jarvislin.domain.entity.UpdateProgress
import com.jarvislin.domain.interactor.DrugstoreUseCase
import com.jarvislin.domain.interactor.ProclamationUseCase
import com.jarvislin.drugstores.extension.bind
import com.jarvislin.drugstores.base.BaseViewModel
import com.jarvislin.drugstores.data.remote.HttpException
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.koin.core.inject
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class MapViewModel : BaseViewModel() {
    private val useCase: DrugstoreUseCase by inject()
    private val proclamationUseCase: ProclamationUseCase by inject()
    val dataPrepared = MutableLiveData<Boolean>(false)
    val drugstoreInfo = MutableLiveData<List<DrugstoreInfo>>(emptyList())
    val progress = MutableLiveData<UpdateProgress>()
    val autoUpdate = MutableLiveData<Boolean>()
    val searchedResult = MutableLiveData<List<DrugstoreInfo>>()
    val statusBarHeight = MutableLiveData<Int>()
    val proclamations = MutableLiveData<Pair<List<Proclamation>, Boolean>>()

    fun fetchOpenData() {
        val subject: PublishSubject<UpdateProgress> = PublishSubject.create()
        subject.toFlowable(BackpressureStrategy.LATEST)
            .zipWith(
                Flowable.interval(600, TimeUnit.MILLISECONDS),
                BiFunction<UpdateProgress, Long, UpdateProgress> { progress, _ -> progress })
            .subscribeBy(
                onNext = { progress.postValue(it) },
                onError = { Timber.e(it) }
            ).bind(this)

        useCase.fetchData(subject)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = { dataPrepared.postValue(true) },
                onError = {
                    when (it) {
                        is IOException -> toastText.postValue("無法連線，請檢查網路狀況")
                        is HttpException -> toastText.postValue("連線錯誤，請稍後再試")
                        else -> toastText.postValue("發生異常，請聯絡作者")
                    }
                    dataPrepared.value = false
                    Timber.e(it)
                })
            .bind(this)
    }

    fun fetchNearDrugstoreInfo(latitude: Double, longitude: Double) {
        if (dataPrepared.value != true) {
            // from on camera idled
            return
        }
        useCase.findNearDrugstoreInfo(latitude, longitude)
            .subscribe({ drugstoreInfo.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }

    fun saveLastLocation(location: Location?) {
        location?.let { useCase.saveLastLocation(it.latitude, it.longitude) }
    }

    fun getLastLocation(): LatLng {
        return useCase.getLastLocation().run { LatLng(first, second) }
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
            .delay(600L, TimeUnit.MILLISECONDS) // for showing progress bar
            .subscribe({ searchedResult.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }

    fun saveStatusBarHeight(height: Int) {
        statusBarHeight.postValue(height)
    }

    fun fetchProclamations() {
        proclamationUseCase.fetchProclamations()
            .subscribe({ proclamations.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }

    fun isFirstLaunch(): Boolean {
        return useCase.isFirstLaunch()
    }

    fun updateFirstLaunch() {
        useCase.updateFirstLaunch()
    }

    fun checkRatingCount() {
        useCase.updateRatingCount()
    }
}
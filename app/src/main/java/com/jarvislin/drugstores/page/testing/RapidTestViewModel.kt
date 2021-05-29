package com.jarvislin.drugstores.page.testing

import androidx.lifecycle.MutableLiveData
import com.jarvislin.domain.entity.*
import com.jarvislin.domain.interactor.RapidTestUseCase
import com.jarvislin.drugstores.base.BaseViewModel
import com.jarvislin.drugstores.data.remote.HttpException
import com.jarvislin.drugstores.extension.bind
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.koin.core.inject
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

class RapidTestViewModel : BaseViewModel() {
    private val useCase: RapidTestUseCase by inject()
    private val locations = MutableLiveData<List<RapidTestLocation>>()
    val cityNames = MutableLiveData<Set<String>>()
    val selectedLocations = MutableLiveData<List<RapidTestLocation>>()
    val progress = MutableLiveData<UpdateProgress>()

    fun fetchRapidTestLocations() {
        useCase.fetchRapidTestLocations()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { progress.value = StartDownloading }
            .observeOn(Schedulers.computation())
            .delay(600L, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                cityNames.value = (it.map { it.city }.toSet())
                it
            }
            .doOnSuccess { progress.value = LatestDataDownloaded }
            .doOnError { progress.value = UpdateFailed }
            .subscribeBy(onSuccess = { locations.postValue(it) }, onError = {
                Timber.e(it)
                when (it) {
                    is IOException -> toastText.postValue("無法連線，請檢查網路狀況")
                    is HttpException -> toastText.postValue("連線錯誤，請稍後再試")
                    else -> toastText.postValue("發生異常，請聯絡作者")
                }
            }).bind(this)
    }

    fun filterLocationByCity(city: String) {
        selectedLocations.postValue(locations.value?.filter { it.city == city } ?: emptyList())
    }
}
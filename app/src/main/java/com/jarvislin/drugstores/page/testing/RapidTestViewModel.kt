package com.jarvislin.drugstores.page.testing

import androidx.lifecycle.MutableLiveData
import com.jarvislin.domain.entity.RapidTestLocation
import com.jarvislin.domain.interactor.RapidTestUseCase
import com.jarvislin.drugstores.base.BaseViewModel
import com.jarvislin.drugstores.data.remote.HttpException
import com.jarvislin.drugstores.extension.bind
import io.reactivex.rxkotlin.subscribeBy
import org.koin.core.inject
import java.io.IOException
import java.util.concurrent.TimeUnit

class RapidTestViewModel : BaseViewModel() {
    private val useCase: RapidTestUseCase by inject()
    private val locations = MutableLiveData<List<RapidTestLocation>>()
    val cityNames = MutableLiveData<Set<String>>()
    val selectedLocations = MutableLiveData<List<RapidTestLocation>>()

    fun fetchRapidTestLocations() {
        useCase.fetchRapidTestLocations()
            .delay(600L, TimeUnit.MILLISECONDS)
            .subscribeBy(onSuccess = {
                locations.postValue(it)
                cityNames.postValue(it.map { it.city }.toSet())
            }, onError = {
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

package com.jarvislin.drugstores.page.detail

import androidx.lifecycle.MutableLiveData
import com.jarvislin.domain.entity.MaskStatus
import com.jarvislin.domain.entity.Status
import com.jarvislin.domain.interactor.DrugstoreUseCase
import com.jarvislin.drugstores.base.BaseViewModel
import com.jarvislin.drugstores.extension.bind
import org.koin.core.inject
import timber.log.Timber
import java.util.*

class DetailViewModel : BaseViewModel() {
    private val useCase: DrugstoreUseCase by inject()
    val latestMaskStatus = MutableLiveData<MaskStatus>()

    fun reportMaskStatus(id: String, status: Status) {
        useCase.reportMaskStatus(id, status)
            .doOnSubscribe { longToastText.postValue("回報成功") }
            .subscribe({ latestMaskStatus.postValue(MaskStatus(status, Date())) }, { Timber.e(it) })
            .bind(this)
    }

    fun fetchMaskStatus(id: String) {
        useCase.fetchMaskStatus(id)
            .subscribe({ latestMaskStatus.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }
}
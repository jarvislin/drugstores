package com.jarvislin.drugstores.page.detail

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.jarvislin.domain.entity.InvalidReportTimeException
import com.jarvislin.domain.entity.MaskRecord
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
    val records = MutableLiveData<List<MaskRecord>>()
    val usesNumberTicket = MutableLiveData<Boolean>()

    fun reportMaskStatus(id: String, status: Status) {
        useCase.reportMaskStatus(id, status)
            .subscribe({
                longToastText.postValue("回報成功")
                latestMaskStatus.postValue(MaskStatus(status, Date()))
            }, {
                if (it is InvalidReportTimeException) {
                    longToastText.postValue("回報過於頻繁，請稍後再試")
                } else {
                    Timber.e(it)
                }
            })
            .bind(this)
    }

    fun fetchMaskStatus(id: String) {
        useCase.fetchMaskStatus(id)
            .subscribe({ latestMaskStatus.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }

    fun reportNumberTicket(id: String) {
        useCase.reportNumberTicket(id)
            .subscribe({
                usesNumberTicket.postValue(true)
                longToastText.postValue("回報成功")
            }, { Timber.e(it) })
            .bind(this)
    }

    fun fetchUsesNumberTicket(id: String) {
        useCase.fetchUsesNumberTicket(id)
            .subscribe({ usesNumberTicket.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }

    fun fetchRecords(id: String) {
        useCase.fetchRecords(id)
            .subscribe({ records.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }
}
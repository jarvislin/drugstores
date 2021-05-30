package com.jarvislin.drugstores.page.menu

import androidx.lifecycle.MutableLiveData
import com.jarvislin.domain.entity.Dashboard
import com.jarvislin.domain.interactor.ConfirmedInfoUseCase
import com.jarvislin.drugstores.base.BaseViewModel
import com.jarvislin.drugstores.extension.bind
import io.reactivex.rxkotlin.subscribeBy
import org.koin.core.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MenuViewModel : BaseViewModel() {
    private val useCase: ConfirmedInfoUseCase by inject()
    val confirmedCase = MutableLiveData<Dashboard>()

    fun fetchCaseData() {
        useCase.fetchConfirmedCase()
            .delay(600L, TimeUnit.MILLISECONDS)
            .subscribeBy(onSuccess = { confirmedCase.postValue(it) }, onError = { Timber.e(it) })
            .bind(this)
    }

}

package com.jarvislin.drugstores.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.koin.core.KoinComponent
import timber.log.Timber
import java.io.IOException

abstract class BaseViewModel : ViewModel(), KoinComponent {
    protected val compositeDisposable = CompositeDisposable()
    val showLoading = MutableLiveData<Boolean>()
    val toastText = MutableLiveData<String>()
    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}
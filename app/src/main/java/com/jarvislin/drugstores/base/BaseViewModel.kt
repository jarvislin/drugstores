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

    protected fun showLoading() {
        showLoading.value = true
    }

    protected fun hideLoading() {
        showLoading.value = false
    }

    protected fun handleError(throwable: Throwable, errorHandler: ErrorHandler? = null) {
        Timber.e(throwable)
        when (throwable) {
            is IOException -> showNoNetworkError()
            is HttpException -> {
                if (errorHandler == null) {
                    when (throwable.code()) {
                        in 400..499 -> show4xxError()
                        in 500..599 -> show5xxError()
                        else -> showUnknownError()
                    }
                }
            }
            else -> showUnknownError()
        }
    }

    private fun showUnknownError() {
        toastText.postValue("發生未知錯誤")
    }

    private fun show4xxError() {
        toastText.postValue("應用程式發生錯誤")
    }

    private fun show5xxError() {
        toastText.postValue("伺服器發生錯誤")
    }

    private fun showNoNetworkError() {
        toastText.postValue("目前未偵測到網路")
    }

}

interface ErrorHandler {
    fun onErrorOccurred(code: String, message: String)
}
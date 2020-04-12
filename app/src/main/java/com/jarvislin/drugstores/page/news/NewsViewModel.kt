package com.jarvislin.drugstores.page.news

import androidx.lifecycle.MutableLiveData
import com.jarvislin.domain.entity.News
import com.jarvislin.domain.interactor.NewsUseCase
import com.jarvislin.drugstores.base.BaseViewModel
import com.jarvislin.drugstores.extension.bind
import org.koin.core.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class NewsViewModel : BaseViewModel() {
    private val newsUseCase by inject<NewsUseCase>()
    val news = MutableLiveData<List<News>>()
    val fetchedFailed = MutableLiveData<Boolean>()

    fun fetchNews() {
        newsUseCase.downloadNews()
            .delay(1, TimeUnit.SECONDS)
            .subscribe({ news.postValue(it) }, {
                fetchedFailed.postValue(true)
                Timber.e(it)
            })
            .bind(this)
    }
}
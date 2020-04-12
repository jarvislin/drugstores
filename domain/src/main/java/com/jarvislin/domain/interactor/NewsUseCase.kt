package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.News
import com.jarvislin.domain.repository.NewsRepository
import io.reactivex.Single

class NewsUseCase(private val newsRepository: NewsRepository) {

    fun downloadNews(): Single<List<News>> = newsRepository.downloadNews()
}
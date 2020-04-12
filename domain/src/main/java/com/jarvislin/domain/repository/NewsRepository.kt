package com.jarvislin.domain.repository

import com.jarvislin.domain.entity.News
import io.reactivex.Single

interface NewsRepository {
    fun downloadNews(): Single<List<News>>

}

package com.jarvislin.drugstores.repository

import com.jarvislin.domain.entity.News
import com.jarvislin.domain.entity.RssException
import com.jarvislin.domain.repository.NewsRepository
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList

class NewsRepositoryImpl : NewsRepository {
    companion object {
        private const val CDC_RSS_FEED =
            "https://www.cdc.gov.tw/RSS/RssXml/Hh094B49-DRwe2RR4eFfrQ?type=1"
    }

    override fun downloadNews(): Single<List<News>> {
        return Single.create<List<News>> { emitter ->
            val rssParser = Parser() // inject it will cause error (it just can only do once)
            rssParser.onFinish(object : Parser.OnTaskCompleted {
                override fun onError() {
                    emitter.onError(RssException())
                }

                override fun onTaskCompleted(articles: ArrayList<Article>) {
                    articles
                        .map { News(it.title, it.description, it.link) }
                        .let { emitter.onSuccess(it) }
                }

            })
            rssParser.execute(CDC_RSS_FEED)
        }.subscribeOn(Schedulers.io())
    }
}
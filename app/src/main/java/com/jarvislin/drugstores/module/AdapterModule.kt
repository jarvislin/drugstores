package com.jarvislin.drugstores.module

import com.jarvislin.drugstores.page.news.NewsAdapter
import com.jarvislin.drugstores.page.proclamation.ProclamationAdapter
import com.jarvislin.drugstores.page.questions.QuestionsAdapter
import com.jarvislin.drugstores.page.search.SearchAdapter
import org.koin.dsl.module

val adapterModule = module {
    factory { SearchAdapter() }
    factory { ProclamationAdapter() }
    factory { QuestionsAdapter() }
    factory { NewsAdapter() }
}
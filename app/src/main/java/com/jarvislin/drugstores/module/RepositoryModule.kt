package com.jarvislin.drugstores.module

import com.jarvislin.domain.repository.ConfirmedInfoRepository
import com.jarvislin.domain.repository.DrugstoreRepository
import com.jarvislin.domain.repository.NewsRepository
import com.jarvislin.domain.repository.ProclamationRepository
import com.jarvislin.drugstores.repository.ConfirmedInfoRepositoryImpl
import com.jarvislin.drugstores.repository.DrugstoreRepositoryImpl
import com.jarvislin.drugstores.repository.NewsRepositoryImpl
import com.jarvislin.drugstores.repository.ProclamationRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<DrugstoreRepository> { DrugstoreRepositoryImpl(get(), get(), get()) }
    factory<ProclamationRepository> { ProclamationRepositoryImpl(get()) }
    factory<NewsRepository> { NewsRepositoryImpl() }
    factory<ConfirmedInfoRepository> { ConfirmedInfoRepositoryImpl(get()) }
}
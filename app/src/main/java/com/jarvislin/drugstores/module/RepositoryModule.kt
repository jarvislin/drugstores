package com.jarvislin.drugstores.module

import com.jarvislin.domain.repository.*
import com.jarvislin.drugstores.repository.*
import org.koin.dsl.module

val repositoryModule = module {
    single<DrugstoreRepository> { DrugstoreRepositoryImpl(get(), get(), get()) }
    factory<ProclamationRepository> { ProclamationRepositoryImpl(get()) }
    factory<NewsRepository> { NewsRepositoryImpl() }
    factory<ConfirmedInfoRepository> { ConfirmedInfoRepositoryImpl(get()) }
    factory<RapidTestRepository> { RapidTestRepositoryImpl(get()) }
    factory<ScanRepository> { ScanRepositoryImpl(get()) }
}
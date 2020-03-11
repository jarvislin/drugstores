package com.jarvislin.drugstores.module

import com.jarvislin.domain.repository.DrugstoreRepository
import com.jarvislin.domain.repository.ProclamationRepository
import com.jarvislin.drugstores.repository.DrugstoreRepositoryImpl
import com.jarvislin.drugstores.repository.ProclamationRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single { DrugstoreRepositoryImpl(get(), get(), get()) as DrugstoreRepository }
    single { ProclamationRepositoryImpl(get()) as ProclamationRepository }
}
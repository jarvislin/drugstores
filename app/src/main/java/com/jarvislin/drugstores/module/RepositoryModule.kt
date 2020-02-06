package com.jarvislin.drugstores.module

import com.jarvislin.domain.repository.DrugstoreRepository
import com.jarvislin.drugstores.repository.DrugstoreRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single { DrugstoreRepositoryImpl(get()) as DrugstoreRepository }
}
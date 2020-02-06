package com.jarvislin.drugstores.module

import com.jarvislin.domain.interactor.DrugstoreUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { DrugstoreUseCase(get()) }
}
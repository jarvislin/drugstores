package com.jarvislin.drugstores.module

import com.jarvislin.domain.interactor.*
import org.koin.dsl.module

val useCaseModule = module {
    single { DrugstoreUseCase(get()) }
    factory { ProclamationUseCase(get()) }
    factory { NewsUseCase(get()) }
    factory { ConfirmedInfoUseCase(get()) }
    factory { RapidTestUseCase(get()) }
}
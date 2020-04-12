package com.jarvislin.drugstores.module

import com.jarvislin.domain.interactor.DrugstoreUseCase
import com.jarvislin.domain.interactor.NewsUseCase
import com.jarvislin.domain.interactor.ProclamationUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { DrugstoreUseCase(get()) }
    single { ProclamationUseCase(get()) }
    single { NewsUseCase(get()) }
}
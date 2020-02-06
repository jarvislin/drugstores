package com.jarvislin.drugstores.module

import com.jarvislin.drugstores.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MapViewModel() }
}
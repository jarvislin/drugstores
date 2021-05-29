package com.jarvislin.drugstores.module

import com.jarvislin.drugstores.page.detail.DetailViewModel
import com.jarvislin.drugstores.page.map.MapViewModel
import com.jarvislin.drugstores.page.menu.MenuViewModel
import com.jarvislin.drugstores.page.news.NewsViewModel
import com.jarvislin.drugstores.page.proclamation.ProclamationViewModel
import com.jarvislin.drugstores.page.testing.RapidTestViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MapViewModel() }
    viewModel { DetailViewModel() }
    viewModel { ProclamationViewModel() }
    viewModel { NewsViewModel() }
    viewModel { MenuViewModel() }
    viewModel { RapidTestViewModel() }
}
package com.jarvislin.drugstores.module

import com.jarvislin.drugstores.page.map.MarkerCacheManager
import com.jarvislin.drugstores.data.LocalData
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
    single { LocalData(androidContext()) }
    factory { MarkerCacheManager() }
}
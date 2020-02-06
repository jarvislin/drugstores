package com.jarvislin.drugstores.module

import com.jarvislin.drugstores.data.remote.NetworkService
import org.koin.dsl.module

val remoteModule = module {
    factory { NetworkService() }
}
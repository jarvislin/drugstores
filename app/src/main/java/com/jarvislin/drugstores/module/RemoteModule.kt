package com.jarvislin.drugstores.module

import com.jarvislin.drugstores.data.remote.Downloader
import org.koin.dsl.module

val remoteModule = module {
    single { Downloader() }
}
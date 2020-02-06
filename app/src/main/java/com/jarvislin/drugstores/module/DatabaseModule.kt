package com.jarvislin.drugstores.module

import com.jarvislin.drugstores.data.db.CustomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { CustomDatabase.getInstance(androidContext()) }
    single { get<CustomDatabase>().drugstoreDao() }
}
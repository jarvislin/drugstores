package com.jarvislin.drugstores.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jarvislin.domain.entity.Drugstore
import com.jarvislin.domain.entity.OpenData

@Database(
    exportSchema = false,
    entities = [
        OpenData::class,
        Drugstore::class
    ],
    version = 1
)

abstract class CustomDatabase : RoomDatabase() {

    abstract fun drugstoreDao(): DrugstoreDao

    companion object {

        @Volatile
        private var INSTANCE: CustomDatabase? = null

        fun getInstance(context: Context): CustomDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                CustomDatabase::class.java, "drugstore.db"
            ).build()
    }
}
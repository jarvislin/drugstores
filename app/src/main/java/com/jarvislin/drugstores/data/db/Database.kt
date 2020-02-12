package com.jarvislin.drugstores.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jarvislin.domain.entity.DrugstoreInfo

@Database(
    exportSchema = false,
    entities = [
        DrugstoreInfo::class
    ],
    version = 2
)

abstract class CustomDatabase : RoomDatabase() {

    abstract fun drugstoreDao(): DrugstoreDao

    companion object {

        private const val DB_NAME = "drugstore.db"

        @Volatile
        private var INSTANCE: CustomDatabase? = null

        fun getInstance(context: Context): CustomDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                CustomDatabase::class.java, DB_NAME
            ).addMigrations(MIGRATION_1_2).build()

    }
}
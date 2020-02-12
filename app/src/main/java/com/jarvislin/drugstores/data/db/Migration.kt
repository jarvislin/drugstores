package com.jarvislin.drugstores.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
                CREATE TABLE DrugstoreInfo (
                    id TEXT PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    lat REAL NOT NULL,
                    lng REAL NOT NULL,
                    update_at TEXT NOT NULL,
                    adult_mask_amount INTEGER NOT NULL,
                    child_mask_amount INTEGER NOT NULL,
                    note TEXT NOT NULL,
                    available TEXT NOT NULL,
                    address TEXT NOT NULL,
                    phone TEXT NOT NULL
                )
                """.trimIndent()
        )
        database.execSQL("DROP TABLE Drugstores")
        database.execSQL("DROP TABLE OpenData")
    }
}
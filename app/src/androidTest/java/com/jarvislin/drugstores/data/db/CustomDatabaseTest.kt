package com.jarvislin.drugstores.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    companion object {
        private const val TEST_DB = "migration-test"
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CustomDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """
                    INSERT INTO Drugstores (id, name, lat, lng, phone, address, note) VALUES('123', '幸福醫院', 25.23456, 121.123456, '02-12345678', '新北市板橋區', '無');
                    INSERT INTO OpenData (drugstore_id, update_at, adult_mask_amount, child_mask_amount) VALUES('123', '2020-02-02', 10, 20);
                    SELECT Drugstores.*, OpenData.* FROM Drugstores INNER JOIN OpenData ON Drugstores.id = OpenData.drugstore_id WHERE  Drugstores.id = '123';
                """.trimIndent()
            )
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
    }
}
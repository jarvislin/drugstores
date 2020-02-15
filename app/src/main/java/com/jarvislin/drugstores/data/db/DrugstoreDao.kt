package com.jarvislin.drugstores.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import com.jarvislin.domain.entity.DrugstoreInfo
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface DrugstoreDao {
    @Query("SELECT * FROM DrugstoreInfo WHERE id = :id")
    fun getDrugstoreInfo(id: String): Single<List<DrugstoreInfo>>

    @Insert(onConflict = IGNORE)
    fun addDrugstoreInfo(shelters: List<DrugstoreInfo>):Completable

    @Query("DELETE FROM DrugstoreInfo")
    fun removeDrugstoreInfo(): Single<Int>

    @Query("SELECT * FROM DrugstoreInfo ORDER BY ABS(:latitude - lat) + ABS(:longitude - lng) ASC LIMIT :limit")
    fun getNearDrugstoreInfo(
        latitude: Double,
        longitude: Double,
        limit: Int
    ): Single<List<DrugstoreInfo>>

    @Query("SELECT * FROM DrugstoreInfo  WHERE address LIKE '%' || :keyword || '%' OR name LIKE '%' || :keyword || '%' LIMIT 300")
    fun getSearchResult(keyword: String): Single<List<DrugstoreInfo>>
}
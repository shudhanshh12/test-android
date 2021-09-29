package `in`.okcredit.dynamicview.data.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Observable

@Dao
interface CustomizationDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg customization: CustomizationEntity)

    @Query("SELECT * FROM CustomizationEntityV2 WHERE businessId = :businessId")
    fun getCustomizations(businessId: String): Observable<List<CustomizationEntity>>

    @Query("DELETE FROM CustomizationEntityV2 WHERE businessId = :businessId")
    suspend fun clearCustomizations(businessId: String)

    @Query("DELETE FROM CustomizationEntityV2")
    suspend fun clearAllCustomizations()
}

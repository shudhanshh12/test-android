package `in`.okcredit.merchant.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import `in`.okcredit.merchant.store.database.BusinessType as DbBusinessType

@Dao
interface BusinessDao {

    @Query("SELECT * FROM Business WHERE id = :businessId")
    fun getBusiness(businessId: String): Observable<Business>

    @Query("SELECT * FROM BusinessCategory")
    fun getCategories(): Flowable<List<BusinessCategory>>

    @Query("SELECT * FROM BusinessType")
    fun getBusinessTypes(): Flowable<List<DbBusinessType>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveMerchant(business: Business)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveCategories(merchant: List<BusinessCategory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveBusinessTypes(businessTypes: List<DbBusinessType>)

    @Query("DELETE FROM Business")
    fun deletedMerchant()

    @Query("DELETE FROM BusinessCategory")
    fun deletedMerchantCategories()

    @Query("SELECT id from Business")
    fun getBusinessIdList(): Flow<List<String>>

    @Query("SELECT * from Business")
    fun getBusinessList(): Observable<List<Business>>

    @Deprecated("To be used only for multiple accounts migration")
    @Query("SELECT id from Business")
    fun getBusinessIdForMultipleAccountsMigration(): List<String>

    @Deprecated("All data from BusinessPreference is migrated to IndividualPreferences")
    @Query("SELECT * FROM BusinessPreference")
    fun getPreferences(): Single<List<BusinessPreference>>
}

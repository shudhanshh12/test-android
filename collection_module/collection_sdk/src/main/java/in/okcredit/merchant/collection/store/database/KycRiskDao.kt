package `in`.okcredit.merchant.collection.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface KycRiskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertKycExternal(kycExternal: KycExternalEntity): Completable

    @Query("SELECT * FROM KycExternalEntity WHERE merchantId == :businessId")
    fun getKycExternalEntity(businessId: String): Observable<KycExternalEntity>

    @Query("SELECT kyc FROM KycExternalEntity WHERE merchantId == :businessId")
    fun getKycStatus(businessId: String): Observable<String>

    @Query("DELETE FROM KycExternalEntity WHERE merchantId == :businessId")
    fun deleteKycData(businessId: String): Completable
}

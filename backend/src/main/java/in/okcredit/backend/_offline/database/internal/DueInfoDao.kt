package `in`.okcredit.backend._offline.database.internal

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime

@Dao
abstract class DueInfoDao {

    @Query("select *  from DueInfo WHERE businessId = :businessId")
    abstract fun getDueInfoList(businessId: String): Observable<List<DbEntities.DueInfo>>

    @Query("select *  from DueInfo where customerId =:customerId and businessId = :businessId limit 1")
    abstract fun getDueInfoForCustomer(customerId: String, businessId: String): Observable<DbEntities.DueInfo>

    @Query("update  DueInfo set is_due_active = 0 where customerId=:customerId and businessId = :businessId")
    abstract fun invalidateDueActiveForCustomer(customerId: String, businessId: String): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDueInfo(vararg dueInfo: DbEntities.DueInfo): Completable

    @Query("update DueInfo set is_custom_date_set =:customDateSet , is_due_active =:dueDateActive  , active_date =:dateTime where customerId=:customerId")
    abstract fun updateCustomDueDateSet(
        customDateSet: Boolean,
        customerId: String,
        dueDateActive: Boolean,
        dateTime: DateTime
    ): Completable

    @Query("update DueInfo set is_due_active =:dueDateActive where customerId=:customerId")
    abstract fun clearDueDateForCustomer(customerId: String, dueDateActive: Boolean = false): Completable

    @Query("select exists (select *  from DueInfo where customerId =:customerId limit 1) ")
    abstract fun isDueInfoExists(customerId: String): Single<Boolean>

    @Query("delete from DueInfo WHERE businessId = :businessId")
    abstract fun clearDueInfo(businessId: String): Completable

    @Query("delete from DueInfo")
    abstract fun deleteAll(): Completable
}
